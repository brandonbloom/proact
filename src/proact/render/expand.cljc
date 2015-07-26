(ns proact.render.expand
  (:require [proact.util :refer [flat]]))

(defn normalize [widget]
  (cond
    (string? widget) {:dom/tag :text :text widget} ;XXX Use :prototype :text
    (map? widget) (update widget :children #(mapv normalize (flat %)))
    :else (throw (ex-info "Unsupported widget type." {:class (type widget)}))))

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

(defn render-template [widget]
  (when-let [template (:template widget)]
    (merge (template widget)
           (select-keys widget [:child-index :id :key :scope]))))

(defn expand [widget]
  (let [widget (-> widget normalize assign-indexes assign-ids)]
    (if-let [widget (render-template widget)]
      (recur widget)
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

;;;;

(def figure
  {:children [{:dom/tag "img"
               :dom/props {"src" "http://example.com/img.jpg"}}
              {:dom/tag :text
               :text "A Thing"}]})


(def display-name
  {:data {:first-name "Brandon"
          :last-name "Bloom"}
   :template (fn [{{:keys [first-name last-name]} :data}]
               {:dom/tag :text
                :text (str last-name ", " first-name)})})

(comment

  (-> "abc"
      expand
      fipp.edn/pprint
      )

  (-> figure
      expand-all
      ;widget->graph
      fipp.edn/pprint
      )

  (-> display-name
      expand-all
      ;widget->graph
      fipp.edn/pprint
      )

  (require 'proact.examples.todo)
  (->
    (proact.examples.todo/app {})
    expand-all
    fipp.edn/pprint
    )

)
