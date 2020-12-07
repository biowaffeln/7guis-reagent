(ns app.seven-guis.util)

(defn create-handler [atom]
  (fn [event]
    (let [value (-> event .-target .-value)]
      (reset! atom value))))

(defn generate-id [] (str (.getTime (js/Date.))))
