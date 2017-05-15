(ns trak.ui.app
  (:require [rum.core :as rum]
            [datascript.core :as d]
            [trak.utils :as utils]
            [trak.db :as db]
            [trak.ui.components :as components]
            [trak.globals :as globals]
            [trak.ui.components.landing :as landing]
            [trak.ui.components.profile :as profile]
            [trak.ui.components.tracks :as tracks]
            ))


;(defn album-artists [artists]
;  (map (fn [artist]
;         [:a {:href (:href artist) :key (:id artist)} (:name artist) [:br]]) artists))
;
;(rum/defc album-card [album]
;  [:.card
;   [:.card-image [:img.img-responsive {:src (:url (first (:album/images album)))}]]
;   [:.card-header [:h4.card-title (:album/name album)]
;    [:h6.card-subtitle (album-artists (:album/artists album))]]
;   [:.card-body "Available markets " (map (fn [m] (str m " ")) (:album/available_markets album))]])
;
;(rum/defc albums [db]
;  (let [albums (db/get-albums db)]
;    [:.columns (map (fn [album] [:.column.col-6.col-xs-12 {:key (:db/id album)} (album-card album)]) albums)]))

;(rum/defc profile-login []
;  (components/link
;    {:href (globals/spotify-authorization-link "user-read-private user-read-email") :class-name "btn btn-primary"}
;    "Log in"))
;
;(rum/defc profile [me]
;  [:div [:h2 (:me/display_name me)] (:me/country me)])
;
;(rum/defc profile-base [db]
;  (let [me (db/me db)]
;    (utils/info me (:me/status me))
;    (cond
;      (= :logged-out (:me/status me)) (profile-login)
;      (= :logged-in (:me/status me)) (profile me)
;      (= :loading (:me/status me)) [:div "Loading..."])))

(rum/defc topbar [db]
  (let [me (db/me db)]
    [:section.section.section-header.bg-gray
     [:section.grid-header.container.grid-960
      [:nav.navbar
       [:section.navbar-section
        (components/link {:href       ""
                          :class-name "navbar-brand mr-10"}
                         (:me/display_name me))]
       [:section.navbar-section (components/link {:href       ""
                                                  :class-name "btn btn-primary"}
                                                 "Log out")]]]]))

(rum/defc sidebar [db]
  [:div.trak-sidebar.column.col-3.col-sm-12 (profile/profile db)])

(rum/defc main [db]
  (let [current-page (:handler (db/app-state db))]
    (cond
      (= :playlist current-page)
      (let [page (db/page-state db)
            status (:application/status page)
            title (:title (:application/state page))]
        [:.column.col-9.col-sm-12.main
         [:h1.text-center title]
         (cond
           (= status :loading) [:.loading]
           (= status :loaded) (tracks/tracks db))]))))

(rum/defc fullpage [db]
  [:section
   (topbar db)
   [:section.container.grid-960
    [:section.columns
     (sidebar db)
     (main db)]]])

(rum/defc app [db]
  (when-let [match (utils/current-path db)]
    (utils/info "Application mounted. Matched: " match)
    (let [status (:me/status (db/me db))]
      (cond
        (= :logged-out status) (landing/landing status)
        (= :loading status) (landing/landing status)
        (= :logged-in status) (fullpage db)
        ))
    ;[:div
    ; (profile-base db)
    ; ;
    ; ;(cond
    ; ;  (empty? (db/me db)) (components/link
    ; ;                         {:href (globals/spotify-authorization-link "user-read-private user-read-email") :class-name "btn btn-primary"}
    ; ;                         "Log in")
    ; ;  :else "noi")
    ; (cond
    ;   (= (:handler (utils/current-path db)) :index) (albums db))])
    ))
