(ns trak.routes
  (:require [goog.events :as events]
            [goog.history.EventType :as EventType]
            [bidi.bidi :as bidi :refer [match-route]]
            [trak.utils :as utils]
            [trak.pubsub :as pubsub])
  (:import goog.history.Html5History
           goog.Uri))

;; -------------------------
;; Bidi routes

(def routes ["/" {"" :index
                  ;"cities/" {[:id] :city}
                  ;"venues/" {[:id] {""                :venue
                  ;                  ["/" :venue-name] :venue}
                  ;           }
                  }
             ])

;; -------------------------
;; History

(defonce history (doto (Html5History.)
                   (.setUseFragment false)
                   (.setPathPrefix "")
                   (.setEnabled true)))

(defn add-transition-listener [callback]
  ; The callbacks are added after the history object has been created
  ; and fired the first event with current location
  ; When a new callback is added, we fire it with the current location
  ; manually
  (callback (match-route routes (.getToken history)))
  (events/listen history
                 EventType/NAVIGATE
                 (fn [event]
                   (callback (match-route routes (.-token event)))))
  )

;; -------------------------
;; Helpers

; Get path from our possible-path
; If such a path matches bidi routes
;  - push to history
;  - prevent event default
; otherwise just fallthrough

(defn navigate [possible-path & [e]]
  (let [path (.getPath (.parse Uri possible-path))]
    (when (match-route routes path)
      (when-let [event e]
        (. event preventDefault)
        (. history (setToken path (.-title (.-target e)))))
      (when-not e
        (. history (setToken path))))))


;; -------------------------
;; Route handlers

(defmulti handler (fn [handler route-params] handler))

(defmethod handler :index [path route-params]
  (utils/info ["Handled path " path route-params])
  ;(pubsub/publish :actions {:action :find-albums})
  )
