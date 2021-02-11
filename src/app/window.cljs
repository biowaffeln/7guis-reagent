(ns app.window
  (:require [reagent.core :as r]))

(defn- win-98-window*
  [props]
  [:div {:class (str "window " (:class props))}
   [:div.title-bar
    [:div.title-bar-text (:title props)]
    [:div.title-bar-controls
     [:a {:href (:link props) :target "_blank"}
      [:button {:aria-label "Help"}]]
     [:button {:aria-label "Close" :on-click (:on-close props)}]]]
   [:div.window-body
    (into [:<>] (r/children (r/current-component)))]])

(def win-98-window (r/reactify-component win-98-window*))