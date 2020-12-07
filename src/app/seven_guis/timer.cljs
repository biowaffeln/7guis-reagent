(ns app.seven-guis.timer
  (:require [reagent.core :as r]
            [app.seven-guis.util :refer [create-handler]]))

(declare duration elapsed-time time-updater reset-timer change-duration)

(defn timer []
  (r/with-let [duration (r/atom 150)
               elapsed-time (r/atom 0)
               time-updater (js/setInterval
                             #(swap! elapsed-time (-> inc)) 100)
               reset-timer #(reset! elapsed-time 0)
               change-duration (create-handler duration)]
    [:div.timer
     [:p "elapsed time" [:progress {:max @duration :value @elapsed-time}]]
     [:p (-> @elapsed-time (min @duration) (/ 10) (.toFixed 1)) "s"]
     [:p "duration" [:input {:type "range" :min 0 :max 300
                             :value @duration
                             :on-change change-duration}]]
     [:button {:on-click reset-timer} "reset timer"]]
    (finally (js/clearInterval time-updater))))
