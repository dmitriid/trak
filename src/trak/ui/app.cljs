(ns trak.ui.app
  (:require [rum.core :as rum]
            [datascript.core :as d]
            [trak.utils :as utils]
            [trak.db :as db]))


(defn album-artists [artists]
  (map (fn [artist]
         [:a {:href (:href artist) :key (:id artist)} (:name artist) [:br]]) artists))

(rum/defc album-card [album]
  [:.card
   [:.card-image [:img.img-responsive {:src (:url (first (:album/images album)))}]]
   [:.card-header [:h4.card-title (:album/name album)]
    [:h6.card-subtitle (album-artists (:album/artists album))]]
   [:.card-body "Available markets " (map (fn [m] (str m " ")) (:album/available_markets album))]])

(rum/defc albums [*db]
  (let [albums (db/get-albums *db)]
    [:.columns (map (fn [album] [:.column.col-6.col-xs-12 {:key (:db/id album)} (album-card album)]) albums)]))

(rum/defc app [*db]
  (when-let [match (utils/current-path *db)]
    (utils/info "Application mounted. Matched: " match)
    (cond
      (= (:handler (utils/current-path *db)) :index) (albums *db))))
