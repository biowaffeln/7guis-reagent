(ns app.seven-guis.counter
  (:require [reagent.core :as r]))

(defn counter []
  (let [click-count (r/atom 0)]
    (fn []
      [:div.counter
       [:input {:type "text" :value @click-count :readOnly true}]
       [:button {:on-click #(swap! click-count inc)} "count"]])))
