(ns trak.ui.components
  (:require [rum.core :as rum]
            [trak.routes :as routes]))


;; spectre.css panel

(rum/defc panel-header [content]
  [:div.panel-header content])

(rum/defc panel [content]
  [:div.panel content])

;;
;; a routable navigating link
;;

;(defcard-doc
;  "##Overview
;
;   ### Links
;
;   ```
;   (link props text)
;   ```
;
;   Props are regular props you would pass to `[:a]`.
;
;   - if `{:on-click some-fn}` is passed, this `some-fn` will be called.
;     **Note**: `some-fn` will have to handle the event itself
;   - if a modifier key is pressed, or a click is anything but left-click,
;     the link will behave as a regular browser link (Cmd+click will open a new
;     browser tab etc.)
;   - if `{:href \"/some/path\"}` *does not* match a path configured in `routes.cljs`,
;     the link will behave as a regular browser link
;   - if `{:href \"/some/path\"}` matches a path configured in `routes.cljs`, it will:
;      - set the path in the browser (`history.pushState`)
;      - will *not* reload the page
;      - will dispatch a proper path to *bidi*
;
;   #### Caveats
;
;   This is wrong:
;
;   ```
;     (link.class1 props text)
;     [link.class1 props text]
;     [link props text]
;   ```
;
;   `link` is just a Rum component, so you'll need to:
;
;   ```
;     (link {:class-name \"class1\"} text)
;     (link {:class-name \"class1\"} text)
;     (link props text)
;   ```
;   "
;  )

(defn- is-not-left-click [e]
  (not (= 0 (.-button e))))

(defn- is-modified [e]
  (or (.-metaKey e) (.-altKey e) (.-ctrlKey e) (.-shiftKey e)))

(defn- create-onclick-handler [props]
  (partial (fn [e]
             (cond
               ; Instead of letting the `<a>` element handle onClick, do it here
               (not (= nil (:on-click props))) ((:on-click props) e)
               ; Don't navigate if custom :on-click called e.preventDefault()
               (= true (.-defaultPrevented e)) nil
               ; If target prop is set (e.g. to "_blank"), let browser handle link.
               (not (= nil (:target props))) nil
               ; The browser will generally only navigate when the user left clicks.
               ; Right clicks or modified clicks have other behaviors, so leave
               ; these behaviors to the browser.
               (or (is-not-left-click e) (is-modified e)) nil
               ; handle it ourselves
               :else (routes/navigate (:href props) e)
               ))))

;
; Usage example
;
;   (link {:href "/a/b/c" :class-name ".btn .btn-primary"} "Click!")
;
(rum/defc link [props children]
  (let [proper-props (merge props {:on-click (create-onclick-handler props)})]
    [:a proper-props children]))


;;;;;;;;;;;;;;;;;;;;
;; Icon


(rum/defc icon [type]
  [:img {:src (str "/img/" type ".svg") :width "16"}])














