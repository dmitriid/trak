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



;; DB Utils

(defn current-path [db]
  (when-let [match (:application/state (first (d/q '[:find [(pull ?e [:application/state])]
                                                     :in $ ?type
                                                     :where [?e :application/state-type ?type]]
                                                   db :path)))]
    {:handler (:handler match) :params (:params match)}))

(defn ifnil? [possibly-nil default]
  (if (nil? possibly-nil) default possibly-nil))

(defn filter-nils [some-sequence]
  (filter (fn [item] (not= nil item)) some-sequence))
