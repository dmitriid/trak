(ns trak.globals
  (:require-macros [trak.config :refer [cljs-env]])
  (:require [trak.utils :as utils])
  (:import goog.Uri
           goog.Uri.QueryData
           goog.string))



(defn build-query-params [kws]
  (utils/info kws)
  (utils/info (.createFromKeysValues QueryData (map name (keys kws)) (vals kws)))
  (reduce #(.add %1
                 (.urlEncode string (name (first %2)))
                 (.urlEncode string (second %2)))
          (QueryData.)
          kws)
  )

(defn spotify-authorization-link [scope]
  (doto (Uri. "https://accounts.spotify.com/authorize")
    (.setQuery (build-query-params {:response_type "code"
                                    :client_id     (cljs-env :spotify-client-id)
                                    :scope         scope
                                    :redirect_uri  (cljs-env :spotify-redirect-url)
                                    :state         (.getRandomString string)}))))
