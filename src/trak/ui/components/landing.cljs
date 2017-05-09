(ns trak.ui.components.landing
  (:require [rum.core :as rum]
            [trak.globals :as globals]
            [trak.ui.components :as components]))

(rum/defc landing [status]
  [:.container
   [:.columns
    [:.column.col-9.centered
     [:img.centered {:src "/img/trak.svg" :width "150px"}]]
    [:.column.col-9.centered.text-center
     (cond
       (= :logged-out status)
       (components/link
         {:href (globals/spotify-authorization-link "user-read-private user-read-email") :class-name "btn btn-primary"}
         "Log in")

       (= :loading status)
       [:.loading])]]])
