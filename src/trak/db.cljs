(ns trak.db
  (:require [datascript.core :as d]
            [cljs.core.async :refer [<!]]
            [trak.api :as api])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Create database
(defonce conn (d/create-conn {:application/state-type {:db/unique :db.unique/identity}
                              :application/state      {}

                              :subscription/name      {}
                              :subscription/channel   {}
                              }))


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
  (unpack-entities db (d/q '[:find ?me
                             :where [?me :me/logged-in _]])))
