(ns app.seven-guis
  (:require
   [app.seven-guis.counter :refer [counter]]
   [app.seven-guis.temperature-converter :refer [temperature-converter]]
   [app.seven-guis.flight-booker :refer [flight-booker]]
   [app.seven-guis.timer :refer [timer]]
   [app.seven-guis.crud :refer [crud]]
   [app.seven-guis.circle-drawer :refer [circle-drawer]]
   [app.seven-guis.cells :refer [cells letters rows]]
   [app.window :refer [win-98-window]]
   [reagent.core :as r]))

(declare components)

(def open (r/atom nil))
(defn on-close [] (reset! open nil))

(def components {:counter [win-98-window {:title "counter"
                                          :link "https://github.com/biowaffeln/seven-guis-reagent/blob/master/src/app/seven_guis/counter.cljs"
                                          :on-close on-close}
                           [counter]]
                 :temp    [win-98-window {:title "temperature converter"
                                          :on-close on-close}
                           [temperature-converter]]
                 :flight  [win-98-window {:title "flight booker"
                                          :on-close on-close}
                           [flight-booker]]
                 :timer   [win-98-window {:title "timer"
                                          :on-close on-close}
                           [timer]]
                 :crud    [win-98-window {:title "crud"
                                          :on-close on-close}
                           [crud]]
                 :draw  [win-98-window {:title "circle-drawer"
                                        :on-close on-close}
                         [circle-drawer]]
                 :spread  [:div
                           [:p.description "Try some of these formulas: `=2*5+5`, `=A5/10`,
                            `=SUM(A1:B5)`, `=5*AVG(A1:A5)`"]
                           [win-98-window {:title "cells" :class "cells"
                                           :on-close on-close}
                            [cells letters rows]]]})


(defn app []
  [:<>
   [:h1 "7GUIs in ClojureScript/Reagent"]
   [:a.about {:href "https://eugenkiss.github.io/7guis/"}
    "about the challenge"]
   [:div.icons
    (for [[name component] components]
      ^{:key name} [:div.icon
                    {:on-click #(reset! open component)}
                    [:img {:src "/icon.png"}]
                    [:p name]])]
   [:div.container
    @open]])
