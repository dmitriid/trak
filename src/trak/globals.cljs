(ns trak.globals
  (:require-macros [trak.config :refer [cljs-env]])
  (:require [trak.utils :as utils]
            [clojure.string :as str])
  (:import goog.Uri
           goog.Uri.QueryData
           goog.string))



(defn build-query-params [kws]
  (utils/info kws)
  (utils/info (.createFromKeysValues QueryData (map name (keys kws)) (vals kws)))
  (reduce #(.add %1
                 (name (first %2))
                 (second %2))
          (QueryData.)
          kws)
  )

(defn spotify-authorization-link [scope]
  (doto (Uri. "https://accounts.spotify.com/authorize")
    (.setQuery (build-query-params {:response_type "token"
                                    :client_id     (cljs-env :spotify-client-id)
                                    :scope         scope
                                    :redirect_uri  (cljs-env :spotify-redirect-url)
                                    :state         (.getRandomString string)}))))

(defn get-spotify-auth-token []
  (let [fragment (.getFragment (Uri. (.-href (.-location js/document))))
        fragment (str/split fragment "&")]
    (reduce (fn [acc part]
              (let [kw (str/split part "=")]
                (merge acc {(keyword (first kw)) (second kw)})))
            {}
            fragment)))
