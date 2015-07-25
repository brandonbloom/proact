(ns proact.components)

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
  (let [widget (-> widget assign-indexes assign-ids)]
    (if-let [widget (render-template widget)]
      (recur widget)
      widget)))

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

  (-> figure
      widget->graph
      fipp.edn/pprint
      )

  (-> display-name
      widget->graph
      fipp.edn/pprint
      )

)
