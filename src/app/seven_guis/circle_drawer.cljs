(ns app.seven-guis.circle-drawer
  (:require [reagent.core :as r]
            [react-dom :refer [createPortal]]
            [app.seven-guis.util :refer [generate-id]]))

(defn make-circle [x y]
  {:x x :y y :radius 30 :id (generate-id)})

(defn add-circle [[x y] state undo-history redo-history]
  (swap! undo-history conj @state)
  (reset! redo-history [])
  (swap! state conj (make-circle x y)))

(defn undo [state undo-history redo-history]
  (swap! redo-history conj @state)
  (reset! state (last @undo-history))
  (swap! undo-history pop))

(defn redo [state undo-history redo-history]
  (swap! undo-history conj @state)
  (reset! state (last @redo-history))
  (swap! redo-history pop))

(defn modal [{id :id} state on-close]
  (let [idx (.indexOf (map :id @state) id)
        initial-state @state
        adjust-selected (r/atom false)
        on-change (fn [event]
                    (let [value (-> event .-target .-value int)]
                      (swap! state assoc-in [idx :radius] value)))
        handle-close (fn [event]
                       (.stopPropagation event)
                       (on-close initial-state))
        modal-elem (.. js/document (getElementById "modal"))]
    (r/create-class
     {:reagent-render
      (fn [modal-state state]
        (let [circle (nth @state idx)]
          (createPortal
           (r/as-element
            [:<>
             [:div {:class "overlay" :on-click handle-close}]
             [:div {:class "modal"
                    :on-click #(.stopPropagation %)
                    :style {:top (:y modal-state) :left (:x modal-state)}}
              (if-not @adjust-selected
                [:button {:on-click #(reset! adjust-selected true)} "adjust diameter"]
                [:div.window
                 [:div.title-bar
                  [:div.title-bar-text ""]
                  [:div {:class "title-bar-controls"}
                   [:button {:aria-label "Close" :on-click handle-close}]]]
                 [:div.window-body
                  [:p "adjust diameter of circle at " (int (:x circle)) ", " (int (:y circle))]
                  [:input {:type "range" :value (:radius circle) :on-change on-change}]]])]])
           modal-elem)))})))

(defn circle-drawer []
  (let [modal-state (r/atom {:id nil :x nil :y nil})
        state (r/atom [])
        undo-history (r/atom [])
        redo-history (r/atom [])

        handle-canvas-click (fn [event]
                              (let [rect (-> event .-target .getBoundingClientRect)
                                    x (- (.-clientX event) (.-left rect))
                                    y (- (.-clientY event) (.-top rect))]
                                (add-circle [x y] state undo-history redo-history)))

        on-undo-click #(undo state undo-history redo-history)
        on-redo-click #(redo state undo-history redo-history)

        handle-circle-click (fn [event id]
                              (.preventDefault event)
                              (reset! modal-state {:id id
                                                   :x (.-pageX event)
                                                   :y (.-pageY event)}))
        modal-close (fn [prev-state]
                      (when-not (= prev-state @state)
                        (swap! undo-history conj prev-state))
                      (reset! modal-state {:id nil :x nil :y nil}))]
    (fn []
      [:div.circle-drawer
       [:p.controls
        [:button {:disabled (empty? @undo-history) :on-click on-undo-click} "undo"]
        [:button {:disabled (empty? @redo-history) :on-click on-redo-click} "redo"]]
       [:div.canvas {:on-click handle-canvas-click}
        (for [{id :id x :x y :y radius :radius} @state]
          ^{:key id} [:div.circle {:id id
                                   :style {:width radius :height radius
                                           :top y :left x}
                                   :on-click #(.stopPropagation %)
                                   :on-context-menu #(handle-circle-click % id)}])
        (when (:id @modal-state)
          [modal @modal-state state modal-close])]])))
