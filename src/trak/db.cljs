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


