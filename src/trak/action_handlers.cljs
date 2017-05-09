(ns trak.action_handlers
  (:require [datascript.core :as d]
            [trak.utils :as utils]
            [trak.api :as api]
            [trak.globals :as globals]
            [trak.db :as db]))


(def api-result-handler)

(defmulti handler (fn [action params *db]
                    (utils/debug "Action handler " action " with params " params)
                    action))

(defmethod handler :find-albums [_ _ _]
  (api/find-albums "blues"))

(defmethod handler :api-call-result [_ params db]
  (api-result-handler (:call params) (:response params) db))

(defmethod handler :login-on-arrival [_ params db]
  (when-let [auth (globals/get-spotify-auth-token)]
    (d/transact! db [(merge {:me/identifier :me
                             :me/status     :loading}
                            (db/convert-atrrs-to-datascript auth :me))])
    (api/me (:access_token auth))
    ))


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
