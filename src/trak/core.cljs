(ns trak.core
  (:require
    [trak.routes :as routes]
    [trak.utils :as utils]
    [trak.pubsub :as pubsub]
    [trak.db :as db]
    [datascript.core :as d]
    [rum.core :as rum]
    [trak.api :as api]
    [trak.ui.app :as ui.app]
    [trak.action_handlers :as action-handlers]
    [trak.pubsub :as pubsub]
    ;[trak.config :as conf :refer [cljs-env]]
    )
  )

(enable-console-print!)

;; Application entry point

(defn app-mount [db]
  (rum/mount (ui.app/app db) (.getElementById js/document "app")))


;; handlers, callbacks, pubsubs


(defn set-current-path-to-db [match *db]
  (let [path-state (d/q '[:find ?e
                          :where [?e :path/state _]] @*db)]
    (when-not path-state
      (d/transact! *db [{:path/state match}]))
    (when path-state
      (d/transact! *db [{:db/id (ffirst path-state) :path/state match}]))))


;; If a subscription with this name to this topic exists,
;; first unsubscribe, then add a new subscription
(defn subscribe-pubsub [*db name topic callback]
  (let [channel (first (d/q '[:find [(pull ?e [:subscription/channel :subscription/topic :db/id])]
                              :in $ ?name
                              :where [?e :subscription/name ?name]]
                            @*db name))]
    (if (nil? channel)
      nil
      (pubsub/unsubscribe (:subscription/topic channel) (:subscription/channel channel)))
    (let [new-channel (pubsub/subscribe
                        topic
                        callback)]
      (let [id (if (nil? channel) -1 (:db/id channel))]
        (d/transact! *db [[:db/add id :subscription/channel new-channel]
                          [:db/add id :subscription/topic topic]
                          [:db/add id :subscription/name name]
                          ])))
    ))

(subscribe-pubsub db/conn :global-listener :path
                  (fn [match]
                    (utils/info "Path subscriber notified: " match)
                    (d/transact! db/conn [{:application/state-type :path
                                           :application/state      match}])
                    (routes/handler (:handler match) (:params match))
                    (utils/info ["subscribe " match])))

(subscribe-pubsub db/conn :actions-listener :actions
                  (fn [match]
                    (utils/info "Actions subscriber dispatched: " (:action match) (:params match))
                    (action-handlers/handler (:action match) (:params match) db/conn)))

;; TODO: Sub/unsub for other callbacks

;; We only want to subscribe once
(when-not (ffirst (d/q '[:find ?e :in $ ?name :where [?e :subscription/name ?name]] @db/conn :transition-listener))
  (d/transact! db/conn [{:subscription/name :transition-listener}])
  (routes/add-transition-listener
    (fn [possible-match]
      (when-let [handler (:handler possible-match)]
        (utils/info ["transition handler callback with " possible-match])
        (pubsub/publish :path possible-match)))))

;; We only want to subscribe once
(when-not (ffirst (d/q '[:find ?e :in $ ?name :where [?e :subscription/name ?name]] @db/conn :db-listener))
  (d/transact! db/conn [{:subscription/name :db-listener}])
  (d/listen! db/conn
             (fn [tx-report]
               ; Careful. Dumping :db-after may overwhelm the console
               ; if you are not careful with transactions
               (utils/info ["DB updated with" (:db-after tx-report)])
               (app-mount (:db-after tx-report)))))
;; Figwheel

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
