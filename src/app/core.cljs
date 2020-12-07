(ns app.core
  (:require [reagent.dom :as r]
            [app.seven-guis :as guis]))

(defn ^:dev/after-load start
  []
  (r/render [guis/app]
            (.getElementById js/document "app")))

(defn ^:export main
  []
  (start))
