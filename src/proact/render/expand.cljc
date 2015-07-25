(ns proact.render.expand
  (:require [proact.util :refer [flat]]))

(defn normalize [widget]
  (cond
    (string? widget) {:html/tag :text :text widget} ;XXX Use :prototype :text
    (map? widget) (update widget :children #(mapv normalize (flat %)))
    :else (throw (ex-info "Unsupported widget type." {:class (class widget)}))))

(defn assign-indexes [widget]
  (update widget :children #(mapv (fn [child i]
                                    (assoc child :child-index i))
                                  % (range))))

(defn assign-ids [widget]
  (let [ctx (:context widget [])
        idx (:child-index widget :root)
        k (:key widget idx)
        id (:id widget [ctx k])
        ctx (conj ctx k)]
    (-> widget
        (assoc :id id)
        (update :children #(mapv (fn [child]
                                   (assign-ids (assoc child :context ctx)))
                                 %)))))

;;XXX Is this necessary? Desirable? Why?
(defn link-children [widget]
  (update widget :children #(mapv :id %)))

(defn render-template [widget]
  (when-let [template (:template widget)]
    (template widget)))

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
  {:children [{:html/tag "img"
               :html/attributes {"src" "http://example.com/img.jpg"}}
              {:html/tag :text
               :text "A Thing"}]})


(def display-name
  {:data {:first-name "Brandon"
          :last-name "Bloom"}
   :template (fn [{{:keys [first-name last-name]} :data}]
               {:html/tag :text
                :text (str last-name ", " first-name)})})

(comment

  (-> "abc"
      expand
      fipp.edn/pprint
      )

  (-> figure
      widget->graph
      fipp.edn/pprint
      )

  (-> display-name
      widget->graph
      fipp.edn/pprint
      )

  (require 'proact.examples.todo)
  (->
    (proact.examples.todo/app {})
    expand-all
    fipp.edn/pprint
    )

)
