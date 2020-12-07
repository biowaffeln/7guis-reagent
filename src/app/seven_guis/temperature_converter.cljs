(ns app.seven-guis.temperature-converter
  (:require [reagent.core :as r]))

(defn temperature-converter []
  (let [temp-c (r/atom nil)
        temp-f (r/atom nil)
        update-f (fn [event]
                   (let [value (-> event .-target .-value)]
                     (reset! temp-f value)
                     (reset! temp-c (-> value (- 32) (* 5) (/ 9) int))))
        update-c (fn [event]
                   (let [value (-> event .-target .-value)]
                     (reset! temp-c value)
                     (reset! temp-f (-> value (* 9) (/ 5) (+ 32) int))))]
    (fn []
      [:div.temperature-converter
       [:input {:type "number" :value @temp-c :on-change update-c}] " Celsius = "
       [:input {:type "number" :value @temp-f :on-change update-f}] " Fahrenheit"])))
