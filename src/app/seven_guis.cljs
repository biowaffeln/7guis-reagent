(ns app.seven-guis
  (:require
   [app.seven-guis.counter :refer [counter]]
   [app.seven-guis.temperature-converter :refer [temperature-converter]]
   [app.seven-guis.flight-booker :refer [flight-booker]]
   [app.seven-guis.timer :refer [timer]]
   [app.seven-guis.crud :refer [crud]]
   [app.seven-guis.circle-drawer :refer [circle-drawer]]
   [app.seven-guis.cells :refer [cells letters rows]]
   [app.window :refer [win-98-window]]))

(defn app []
  [:<>
   [:h1 "7GUIs in ClojureScript/Reagent"]
   [:div.container

    [win-98-window {:title "counter"}
     [counter]]
    [win-98-window {:title "temperature converter"}
     [temperature-converter]]
    [win-98-window {:title "flight booker"}
     [flight-booker]]
    [win-98-window {:title "timer"}
     [timer]]
    [win-98-window {:title "crud"}
     [crud]]
    [win-98-window {:title "circle-drawer"}
     [circle-drawer]]
    [win-98-window {:title "cells" :class "cells"}
     [cells letters rows]]]])
