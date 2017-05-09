(ns trak.db
  (:require [datascript.core :as d]
            [cljs.core.async :refer [<!]]
            [trak.api :as api])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Create database

(defonce schema {:application/state-type {:db/unique :db.unique/identity}
                 :application/state      {}

                 :me/identifier          {:db/unique :db.unique/identity}
                 :me/status              {}

                 :subscription/name      {}
                 :subscription/channel   {}

                 :playlists/owner        {:db/unique :db.unique/identity}
                 })

(defn- create-database []
  (let [db (d/create-conn schema)]
    (d/transact! db [{:me/identifier :me
                      :me/status     :logged-out}])
    db))

(defonce conn (create-database))

;
; If you have a collection of entites (as it's usually
; returned by datascript queries), unpack them into entities
; (so we have (1 2 3) instead of #{[1] [2] [3]} etc.)
;
(defn unpack-entities [db collection-of-ids]
  (->> collection-of-ids
       (map first)
       (map #(d/entity db %))))


;; Data retrieval

(defn get-albums [db]
  (unpack-entities db (d/q '[:find ?album
                             :where [?album :album/id _]]
                           db)))

(defn me [db]
  (d/entity db (ffirst (d/q '[:find ?me
                              :in $ ?identity
                              :where [?me :me/identifier ?identity]] db :me))))

(defn playlists [db owner_id]
  (unpack-entities db (d/q '[:find ?playlist
                             :in $ ?owner_id
                             :where [?playlist :playlist/owner ?owner]]
                           db owner_id)))

;; Utilities

;; Convert prefix and key to :prefix/key to use with Datascript
(defn to-prefixed-keyword [prefix key]
  (cond
    (= key :db/id) key
    :else (keyword (name prefix) (name key))))

;; Convert {:name "event" :startTime "1234"} to
;;         {:event/name "event" :event/startTime "1234}
;; etc.

(defn convert-atrrs-to-datascript [attrs keyword-prefix]
  (reduce #(assoc %1 (to-prefixed-keyword keyword-prefix (first %2)) (second %2)) {} attrs))

(defn json-to-datascript [collection-of-entities keyword-prefix]
  (map (fn [entity]
         (convert-atrrs-to-datascript entity keyword-prefix))
       collection-of-entities))
