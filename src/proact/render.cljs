(ns proact.render)

;;TODO create the widget tree (dag? graph?) here
;; when mounting things, attach listeners:
;;    all at root, some local (like submit)
(defonce state (atom {:roots {} :mounts {}}))

(defn flat [xs]
  (lazy-seq
    (when-first [x xs]
      (let [xs* (flat (next xs))]
        (cond
          (nil? x) xs*
          (seq? x) (concat (flat x) xs*)
          :else (cons x xs*))))))

(def ^:dynamic *render*)

(declare render)

(defn create-text [text]
  (.createTextNode js/document text))

(defn create-element [{:as widget}]
  (let [el (.createElement js/document (:html/tag widget))]
    (doseq [[k v] (:html/attributes widget)]
      (aset el k v))
    (doseq [child (-> widget :children flat)]
      (render el child))
    el))

(defn render [parent {tag :html/tag :keys [template text] :as widget}]
  (cond
    (nil? widget) nil
    (string? widget) (.appendChild parent (create-text widget))
    template (recur parent (template widget))
    text (recur parent (-> widget
                         (dissoc :text)
                         (assoc :children [(str text)])))
    tag (.appendChild parent (create-element widget))
    :else (assert false (str "TODO: just recurse?" widget))))

(defn render-root [id widget]
  (binding [*render* :TODO]
    (let [el (.getElementById js/document id)]
      (while (pos? (alength (.-children el)))
        (.removeChild el (.-firstChild el)))
      (render el widget))))
