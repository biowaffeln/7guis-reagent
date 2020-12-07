(ns app.seven-guis.flight-booker
  (:require [reagent.core :as r]
            [app.seven-guis.util :refer [create-handler]]))

(defn flight-booker []
  (let [flight-type (r/atom "one-way")
        today (-> (js/Date.) .toISOString (subs 0 10))
        start-date (r/atom today)
        end-date (r/atom today)
        change-start-date (create-handler start-date)
        change-end-date (create-handler end-date)]
    (fn []
      [:div.flight-booker
       [:select {:value @flight-type :on-change (create-handler flight-type)}
        [:option {:value "one-way"} "one-way flight"]
        [:option {:value "return"} "return flight"]]
       [:input {:type "date"
                :value @start-date
                :on-change change-start-date}]
       [:input {:type "date"
                :value @end-date
                :on-change change-end-date
                :disabled (= @flight-type "one-way")}]
       [:button {:disabled (and (= @flight-type "return")
                                (> @start-date @end-date))
                 :on-click #(if (= @flight-type "one-way")
                              (js/alert (str "You have booked a one-way flight for " @start-date))
                              (js/alert (str "You have booked a return flight from " @start-date " to " @end-date)))}
        "book"]])))
