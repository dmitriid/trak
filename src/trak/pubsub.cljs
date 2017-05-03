(ns trak.pubsub
  (:require [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))


(defonce event-bus (async/chan))
(defonce event-bus-pub (async/pub event-bus first))

(defn publish [key message]
  (async/put! event-bus [key message]))

(defn subscribe [key callback]
  (let [ch (async/chan)]
    (async/sub event-bus-pub key ch)
    (go-loop []
             (let [[_ msg] (<! ch)]
               (callback msg))
             (recur))
    ch))

(defn unsubscribe [topic channel]
  (async/unsub event-bus-pub topic channel))
