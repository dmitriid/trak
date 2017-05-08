(ns trak.api
  (:require [trak.utils :as utils]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [trak.pubsub :as pubsub])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [trak.config :refer [cljs-env]]))




(defn- api-call
  ([method url callback] (api-call method url {} callback))
  ([method url other callback]
   (go (let [response (<! (method url other))]
         (callback response)))
    ))


(defn find-albums [string]
  (api-call (partial http/get)
            "https://api.spotify.com/v1/search"
            {:query-params      {:q string :type "album"}
             :with-credentials? false}
            (fn [response] (pubsub/publish :actions {:action :api-call-result
                                                     :params {:call     :find-albums
                                                              :response response}})))
  )

(defn me [auth]
  (api-call (partial http/get)
            "https://api.spotify.com/v1/me"
            {:with-credentials? false
             :oauth-token       auth}
            (fn [response] (pubsub/publish :actions {:action :api-call-result
                                                     :params {:call     :me
                                                              :response response}}))))
