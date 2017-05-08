(ns trak.action_handlers
  (:require [datascript.core :as d]
            [trak.utils :as utils]
            [trak.api :as api]))


(def api-result-handler)

(defmulti handler (fn [action params *db] action))

(defmethod handler :find-albums [_ _ _]

  (api/find-albums "blues"))

(defmethod handler :api-call-result [_ params db]
  (utils/info "Api call result " params)
  (api-result-handler (:call params) (:response params) db))


;; Utitlity. Convert prefix and key to :prefix/key to use with Datascript
(defn to-prefixed-keyword [prefix key]
  (cond
    (= key :db/id) key
    :else (keyword (name prefix) (name key))))

;; Convert {:name "event" :startTime "1234"} to
;;         {:event/name "event" :event/startTime "1234}
;; etc.

(defn- convert-atrrs-to-datascript [attrs keyword-prefix]
  (reduce #(assoc %1 (to-prefixed-keyword keyword-prefix (first %2)) (second %2)) {} attrs))

(defn json-to-datascript [collection-of-entities keyword-prefix]
  (map (fn [entity]
         (convert-atrrs-to-datascript entity keyword-prefix))
       collection-of-entities))


(defmulti api-result-handler (fn [api result *db] api))

(defmethod api-result-handler :find-albums [action response *db]
  (utils/info "Action received " action response)
  (when-let [{{{albums :items} :albums} :body} response]
    (d/transact! *db (json-to-datascript albums :album))))
    (d/transact! *db (db/json-to-datascript albums :album))))
