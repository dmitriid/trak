(ns trak.action_handlers
  (:require [datascript.core :as d]
            [trak.utils :as utils]
            [trak.api :as api]
            [trak.globals :as globals]
            [trak.db :as db]))


(def api-result-handler)

(defmulti handler (fn [action params *db] action))

(defmethod handler :find-albums [_ _ _]
  (api/find-albums "blues"))

(defmethod handler :api-call-result [_ params db]
  (utils/info "Api call result " params)
  (api-result-handler (:call params) (:response params) db))

(defmethod handler :login-on-arrival [_ params db]
  (utils/info "Trying to log in user on arrival " params)
  (when-let [auth (globals/get-spotify-auth-token)]
    (utils/info db)
    (d/transact! db [{:me/identifier :me
                      :me/status     :loading}])
    (d/transact! db [(merge {:me/identifier :me}
                            (db/convert-atrrs-to-datascript auth :me))])
    (utils/info (:access_token auth))
    (api/me (:access_token auth))))


(defmulti api-result-handler (fn [api result *db] api))

(defmethod api-result-handler :find-albums [action response *db]
  (utils/info "Action received " action response)
  (when-let [{{{albums :items} :albums} :body} response]
    (d/transact! *db (json-to-datascript albums :album))))
    (d/transact! *db (db/json-to-datascript albums :album))))
