(ns trak.action_handlers
  (:require [datascript.core :as d]
            [trak.utils :as utils]
            [trak.api :as api]
            [trak.globals :as globals]
            [trak.db :as db])
  (:import goog.string))


(def api-result-handler)

(defmulti handler (fn [action params *db]
                    (utils/debug "Action handler " action " with params " params)
                    action))

(defmethod handler :find-albums [_ _ _]
  (api/find-albums "blues"))

(defmethod handler :api-call-result [_ params *db]
  (api-result-handler (:call params) (:response params) *db))

(defmethod handler :login-on-arrival [_ params *db]
  (when-let [auth (globals/get-spotify-auth-token)]
    (d/transact! *db [(merge {:me/identifier :me
                              :me/status     :loading}
                             (db/convert-atrrs-to-datascript auth :me))])
    (api/me (:access_token auth))))

(defmethod handler :load-playlist [_ params *db]
  (d/transact! *db (map (fn [track] [:db.fn/retractEntity (:db/id track)]) (db/tracks @*db)))
  (d/transact! *db [{:application/state-type :page
                     :application/state      {:title (:playlist/name (db/playlist @*db (:id params)))}
                     :application/status     :loading}])
  (let [id (:id params)]
    (when-let
      [tracks (:playlist/tracks (db/playlist @*db id))]
      (api/playlist (:href tracks) (:me/access_token (db/me @*db))))))

(defmulti api-result-handler (fn [api result *db]
                               (utils/debug "API result handler " api " with result " result)
                               api))

(defmethod api-result-handler :find-albums [action response *db]
  (when-let [{{{albums :items} :albums} :body} response]
    (d/transact! *db (db/json-to-datascript albums :album))))

(defmethod api-result-handler :me [action response *db]
  (cond
    (= 401 (:status response)) (d/transact! *db [{:me/identifier :me :me/status :logged-out}])
    (= 200 (:status response))
    (do
      (d/transact! *db [(merge {:me/identifier :me :me/status :logged-in}
                               (db/convert-atrrs-to-datascript (:body response) :me))])
      (let [me (db/me @*db)]
        (api/user-playlists (:me/href me) (:me/access_token me))))))

(defmethod api-result-handler :user-playlists [action response *db]
  (let [body (:body response)
        items (:items body)
        db-playlists {:playlists/limit    (:limit body)
                      :playlists/next     (utils/ifnil? (:next body) -1)
                      :playlists/offset   (:offset body)
                      :playlists/previous (utils/ifnil? (:previous body) -1)
                      :playlists/total    (:total body)
                      :playlists/owner    :me}
        owned-items (map (fn [item] (merge item {:owner :me})) items)
        db-items (db/json-to-datascript owned-items :playlist)
        ]
    (d/transact! *db [db-playlists])
    (d/transact! *db db-items)
    ))

(defmethod api-result-handler :playlist-tracks [action response *db]
  (cond
    (= 200 (:status response))
    (let [body (:body response)
          items (:items body)
          track-items (map (fn [item]
                             (let [track (:track item)
                                   album (:album track)
                                   artists (:artists track)]
                               {:name         (:name track)
                                :href         (utils/ifnil? (:href track) "")
                                :id           (utils/ifnil? (:id track) (.getRandomString string))
                                :track-number (utils/ifnil? (:track_number track) -1)
                                :album        (utils/ifnil? (:name album) "")
                                :album-id     (utils/ifnil? (:id album) (.getRandomString string))
                                :album-href   (utils/ifnil? (:href album) "")
                                :album-images (:images album)
                                :artists      (map (fn [artist] {:name (:name artist) :href (:href artist) :id (:id artist)}) artists)
                                :duration-ms  (:duration_ms track)
                                :popularity   (:popularity track)
                                })) items)
          db-items (db/json-to-datascript track-items :track)]
      (d/transact! *db db-items)
      (d/transact! *db [{:application/state-type :page :application/status :loaded}]))))
