(ns trak.config
  (:require [environ.core :refer [env]]))


(defmacro cljs-env [kw]
  (env kw))
