(ns app.seven-guis.cells
  (:require
   [reagent.core :as r]
   [clojure.string :as str]
   [instaparse.core :as insta]
   [cljs.core.match :refer-macros [match]]
   [app.seven-guis.util :refer [create-handler]]))

(defn- pos->keyword [letter row]
  (keyword (str letter row)))

(defn- init-cells [cols rows]
  (->>
   (for [col cols row rows]
     (list (pos->keyword col row) {:expr "" :value nil}))
   (flatten)
   (apply hash-map)))

(def parser
  (insta/parser
   "<expr> = add-expr | mult-expr | number | cell
	  <val>  = number | cell
    add-expr  = expr (('+' | '-') expr)
		mult-expr = val ( ('*' | '/') (mult-expr|val) )
    number = #'[+-]?([0-9]*[.])?[0-9]+'
    cell = #'[A-Z][0-9]+'"
   :auto-whitespace (insta/parser "WS = #'\\s+'")))

(declare e1 e2 cell)

(defn- eval-expr [expr state]
  (match expr
    [:add-expr e1 "+" e2]  (+ (eval-expr e1 state)
                              (eval-expr e2 state))
    [:add-expr e1 "-" e2]  (- (eval-expr e1 state)
                              (eval-expr e2 state))
    [:mult-expr e1 "*" e2] (* (eval-expr e1 state)
                              (eval-expr e2 state))
    [:mult-expr e1 "/" e2] (/ (eval-expr e1 state)
                              (eval-expr e2 state))
    [:cell e1] (js/parseFloat @(r/cursor state [:cells (keyword e1) :value]))
    [:number e1] (js/parseFloat e1)))

(defn- parse-expr [expr state]
  (try
    (-> expr
        (parser)
        (first)
        (eval-expr state))
    (catch :default e
      (js/console.log e)
      "ERROR")))

(defn- parse [value state]
  (if (str/starts-with? value "=")
    (-> value (subs 1) (parse-expr state) str)
    value))

(defn- cell [state key]
  (let [expr (r/cursor state [:cells key :expr])]
    (fn []
      ;; (js/console.log (str key " rerendered"))
      (let [value (parse @expr state)]
        (swap! state assoc-in [:cells key :value] value)
        [:td
         [:div.cell
          {:on-click #(swap! state assoc :active key)}
          value]]))))

(defn- active-cell [state key]
  (let [expr (r/atom (get-in @state [:cells key :expr]))]
    (fn []
      [:td.active
       [:div.cell
        [:input {:auto-focus true
                 :value @expr
                 :on-change (create-handler expr)
                 :on-blur (fn []
                            (swap! state assoc :active nil)
                            (swap! state assoc-in [:cells key :expr] @expr))}]]])))

(def rows (range 100))
(def letters (map char (range 65 91)))

(defn cells [letters rows]
  (let [state (r/atom {:active nil
                       :cells (init-cells letters rows)})]
    (fn []
      (let [active-key (:active @state)]
        [:div.spreadsheet>table
         [:thead [:tr [:th ""] (for [letter letters]
                                 ^{:key letter} [:th letter])]]
         [:tbody
          (for [row rows] ^{:key row}
               [:tr [:td [:span row]]
                (for [letter letters
                      :let [key (pos->keyword letter row)]]
                  ^{:key (str letter row)} [:<>
                                            (if (= key active-key)
                                              [active-cell state key]
                                              [cell state key])])])]]))))
