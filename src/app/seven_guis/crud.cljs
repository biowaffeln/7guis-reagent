(ns app.seven-guis.crud
  (:require [reagent.core :as r]
            [react-dom]
            [app.seven-guis.util :refer [create-handler generate-id]]
            [clojure.string :as str]))

(defn crud []
  (let [filter-prefix (r/atom "")
        users (r/atom [{:id "123"  :name "Max" :surname "Mustermann"}
                       {:id "321"  :name "Mark" :surname "Kvetny"}
                       {:id "cool"  :name "Cool" :surname "Dude"}])
        select-state (r/atom "")
        new-user (r/atom {:name "" :surname ""})

        handle-name-change    (fn [event]
                                (swap! new-user assoc :name    (-> event .-target .-value)))
        handle-surname-change (fn [event]
                                (swap! new-user assoc :surname (-> event .-target .-value)))

        change-filter-prefix (create-handler filter-prefix)
        change-select-state (create-handler select-state)

        create-fn (fn []
                    (swap! users conj (assoc @new-user :id (generate-id)))
                    (reset! new-user {:name "" :surname ""}))
        delete-fn (fn []
                    (swap! users #(->> % (remove (fn [user]
                                                   (= (:id user) @select-state))) vec))
                    (reset! select-state ""))
        update-fn (fn []
                    (let [idx (.indexOf (map :id @users) @select-state)]
                      (swap! users assoc-in [idx :name] (:name @new-user))
                      (swap! users assoc-in [idx :surname] (:surname @new-user))))]
    (fn []
      (let [filtered-users (doall (filter
                                   (fn [{name :name surname :surname}]
                                     (or (str/starts-with? name @filter-prefix)
                                         (str/starts-with? surname @filter-prefix)))
                                   @users))
            disabled (or (empty? filtered-users)
                         (= @select-state ""))]
        [:div.crud
         [:section
          [:p.filter [:label "filter prefix: "]
           [:input {:type "text" :value @filter-prefix :on-change change-filter-prefix}]]
          [:select {:size 5
                    :on-change change-select-state
                    :on-click change-select-state}
           (for [{name :name surname :surname id :id} filtered-users]
             ^{:key id} [:option {:value id} name ", " surname])]
          [:div.user-inputs
           [:label "name: "]
           [:input {:type "text" :value (:name @new-user) :on-change handle-name-change}]
           [:label "surname: "]
           [:input {:type "text" :value (:surname @new-user) :on-change handle-surname-change}]]]

         [:p.controls
          [:button {:on-click create-fn} "create"]
          [:button {:on-click update-fn :disabled disabled} "update"]
          [:button {:on-click delete-fn :disabled disabled} "delete"]]]))))
