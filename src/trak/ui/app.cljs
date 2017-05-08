(ns trak.ui.app
  (:require [rum.core :as rum]
            [datascript.core :as d]
            [trak.utils :as utils]
            [trak.db :as db]
            [trak.ui.components :as components]
            [trak.globals :as globals]))


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

(rum/defc profile-login []
  (components/link
    {:href (globals/spotify-authorization-link "user-read-private user-read-email") :class-name "btn btn-primary"}
    "Log in"))

(rum/defc profile [me]
  [:div [:h2 (:me/display_name me)] (:me/country me)])

(rum/defc profile-base [*db]
  (let [me (db/me *db)]
    (utils/info me (:me/status me))
    (cond
      (nil? me) (profile-login)
      (= :logged-out (:me/status me)) (profile-login)
      (= :logged-in (:me/status me)) (profile me)
      (= :loading (:me/status me)) [:div "Loading..."])))

(rum/defc app [*db]
  (when-let [match (utils/current-path *db)]
    (utils/info "Application mounted. Matched: " match)
    [:div
     (profile-base *db)
     ;
     ;(cond
     ;  (empty? (db/me *db)) (components/link
     ;                         {:href (globals/spotify-authorization-link "user-read-private user-read-email") :class-name "btn btn-primary"}
     ;                         "Log in")
     ;  :else "noi")
     (cond
       (= (:handler (utils/current-path *db)) :index) (albums *db))]))
