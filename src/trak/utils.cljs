(ns trak.utils
  (:require [datascript.core :as d]))


(defn js-apply [f target args]
  (.apply f target (to-array args)))


;; Console-related stuff

(defn log [& args]
  (js-apply (.-log js/console) js/console args))

(defn info [& args]
  (js-apply (.-info js/console) js/console args))

(defn debug [& args]
  (js-apply (.-debug js/console) js/console args))

(defn error [& args]
  (js-apply (.-error js/console) js/console args))



