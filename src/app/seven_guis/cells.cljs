(ns app.seven-guis.cells
  (:require
   [reagent.core :as r]
   [clojure.string :as str]
   [instaparse.core :as insta]
   [cljs.core.match :refer-macros [match]]
   [app.seven-guis.util :refer [create-handler]]))

(defn- pos->keyword [letter row]
  (keyword (str letter row)))

(defn- keyword->pos [kw]
  (list
   (second (str kw))
   (js/parseInt (subs (str kw) 2))))

(defn- char->int [ch]
  (-> ch (.charCodeAt 0)))

(defn- cell-range [c1 c2]
  (let [[c1-letter c1-row] (keyword->pos c1)
        [c2-letter c2-row] (keyword->pos c2)
        row-range (range c1-row (inc c2-row))
        letter-range (map char (range (char->int c1-letter) (inc (char->int c2-letter))))]
    (for [col letter-range row row-range]
      (pos->keyword col row))))

(defn- init-cells [cols rows]
  (->>
   (for [col cols row rows]
     (list (pos->keyword col row) {:expr "" :value nil}))
   (flatten)
   (apply hash-map)))

(def parser
  (insta/parser
   "<expr> = add-expr | range-expr | mult-expr | number | cell
	  <val>  = number | cell | range-expr
    add-expr  = expr (('+' | '-') expr)
		mult-expr = val ( ('*' | '/') (mult-expr|val) )
		range-expr = ('SUM' | 'AVG') '(' cell ':' cell ')'
    number = #'[+-]?([0-9]*[.])?[0-9]+'
    cell = #'[A-Z][0-9]+'"
   :auto-whitespace (insta/parser "WS = #'\\s+'")))

(defn get-cell-value [queried-cell original-cell state]
  (if (= original-cell queried-cell)
    (throw (js/Error. "recursion"))
    (let [val @(r/cursor state [:cells queried-cell :value])]
      (if (empty? val)
        0 (js/parseFloat val)))))

(defn calc-sum [cell1 cell2 original-cell state]
  (let [cells (cell-range (keyword cell1) (keyword cell2))]
    (->> cells
         (map #(get-cell-value % original-cell state))
         (reduce +))))

(defn calc-avg [cell1 cell2 original-cell state]
  (let [cells (cell-range (keyword cell1) (keyword cell2))]
    (->> cells
         (map #(get-cell-value % original-cell state))
         (reduce +)
         (* (/ 1 (count cells))))))

(declare e1 e2 cell _)

(defn- eval-expr [expr original-cell state]
  (match expr
    [:add-expr e1 "+" e2]  (+ (eval-expr e1 original-cell state)
                              (eval-expr e2 original-cell state))
    [:add-expr e1 "-" e2]  (- (eval-expr e1 original-cell state)
                              (eval-expr e2 original-cell state))
    [:mult-expr e1 "*" e2] (* (eval-expr e1 original-cell state)
                              (eval-expr e2 original-cell state))
    [:mult-expr e1 "/" e2] (/ (eval-expr e1 original-cell state)
                              (eval-expr e2 original-cell state))
    [:range-expr "SUM" _ e1 _ e2 _] (calc-sum (second e1) (second e2) original-cell state)
    [:range-expr "AVG" _ e1 _ e2 _] (calc-avg (second e1) (second e2) original-cell state)
    [:cell e1] (get-cell-value (keyword e1) original-cell state)
    [:number e1] (js/parseFloat e1)))

(defn- parse-expr [expr original-cell state]
  (try
    (-> expr
        (parser)
        (first)
        (eval-expr original-cell state))
    (catch :default e
      ;; (js/console.log e)
      (if (= (.. e -message) "recursion")
        "RECURSION"
        "ERROR"))))

(defn- parse [value original-cell state]
  (if (str/starts-with? value "=")
    (-> value (subs 1) (parse-expr original-cell state) str)
    value))

(defn- cell [state key]
  (let [expr (r/cursor state [:cells key :expr])]
    (fn []
      ;; (js/console.log (str key " rerendered"))
      (let [value (parse @expr key state)]
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
