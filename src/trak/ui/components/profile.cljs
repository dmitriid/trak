(ns trak.ui.components.profile
  (:require [rum.core :as rum]
            [trak.utils :as utils]
            [trak.db :as db]
            [trak.globals :as globals]
            [trak.ui.components :as components]))


;(rum/defc profile-login []
;  (components/link
;    {:href (globals/spotify-authorization-link "user-read-private user-read-email") :class-name "btn btn-primary"}
;    "Log in"))
;
;(rum/defc profile [me]
;  [:div [:h2 (:me/display_name me)] (:me/country me)])
;
;(rum/defc profile-base [*db]
;  (let [me (db/me *db)]
;    (utils/info me (:me/status me))
;    (cond
;      (= :logged-out (:me/status me)) (profile-login)
;      (= :logged-in (:me/status me)) (profile me)
;      (= :loading (:me/status me)) [:div "Loading..."])))


(defn profile-playlists [db]
  (let [playlists (db/playlists db :me)]
    (map (fn [playlist]
           [:li.nav-item {:key (:playlist/id playlist)}
            (components/icon "web")
            (components/link {:href (str "/playlists/" (:playlist/id playlist))}
                             (:playlist/name playlist))])
         playlists))
  )

(rum/defc profile [db]
  [:ul.sidebar-nav.nav
   [:li.nav-item.active "Playlists" [:ul.nav (profile-playlists db)]]])
