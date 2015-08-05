(ns proact.render.expand
  (:require [proact.util :refer [flat]]))

(defn normalize [widget]
  (cond
    (string? widget) {:dom/tag :text :text widget} ;XXX Use :prototype :text
    (map? widget) (update widget :children #(mapv normalize (flat %)))
    :else (throw (ex-info "Unsupported widget type." {:class (type widget)}))))

(defn inherit-prototype [widget]
  (merge (:prototype widget) (dissoc widget :prototype)))

(defn assign-indexes [widget]
  (update widget :children #(mapv (fn [child i]
                                    (-> child
                                        (assoc :child-index i)
                                        assign-indexes))
                                  % (range))))

(defn assign-ids [widget]
  (let [scope (:scope widget [])
        idx (:child-index widget :root)
        k (:key widget idx)
        id (:id widget [scope k])
        scope (conj scope k)]
    (-> widget
        (assoc :id id)
        (update :children #(mapv (fn [child]
                                   (assign-ids (assoc child :scope scope)))
                                 %)))))

;;XXX Is this necessary? Desirable? Why?
(defn link-children [widget]
  (update widget :children #(mapv :id %)))

(def default-item-prototype
  {:template
   (fn [x]
     {:dom/tag :text ;XXX
      :text (pr-str (:data x))})})

(defn render-items [widget]
  (let [filt (:item-filter widget (constantly true))]
    (if-let [items (->> widget :items (filter filt) seq)]
      (let [proto (:item-prototype widget default-item-prototype)]
        (assoc widget :children (mapv #(assoc proto :data %) items)))
      widget)))

(defn render-template [widget]
  (when-let [template (:template widget)]
    (merge (template widget)
           (select-keys widget [:child-index :id :key :scope]))))

(defn expand [widget]
  (let [widget (-> widget
                   normalize
                   inherit-prototype
                   render-items
                   assign-indexes
                   assign-ids)]
    (if-let [rendered (render-template widget)]
      (merge (select-keys widget [:data :handler])
             {:children [(expand rendered)]})
      widget)))

(defn expand-all [widget]
  (-> widget
      expand
      (update :children #(mapv expand-all %))))

;;; Graph stuff?

(defn add-widget [graph widget]
  (assoc graph (:id widget) widget))

(defn add-widget-tree [graph widget]
  (let [widget (expand widget)
        graph (reduce add-widget-tree
                      graph
                      (:children widget))]
    (add-widget graph (link-children widget))))

(def null {})

(defn widget->graph [widget]
  (add-widget-tree null widget))
