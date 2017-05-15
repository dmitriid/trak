(ns trak.ui.components.tracks
  (:require [rum.core :as rum]
            [trak.utils :as utils]
            [trak.db :as db]
            [trak.globals :as globals]
            [trak.ui.components :as components])
  (:import goog.string))


(rum/defc render-track [track]
  [:.tile
   [:.tile-icon ""]
   [:.tile-content
    [:p.tile-title (:track/name track)]
    [:p.tile-subtitle (:track/album track)]
    ]])

(defn tracks [db]
  (map (fn [track] (rum/with-key (render-track track) (.getRandomString string))) (db/tracks db)))
