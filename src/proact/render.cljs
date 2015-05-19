(ns proact.render)

(defn flat [xs]
  (lazy-seq
    (when-first [x xs]
      (let [xs* (flat (next xs))]
        (cond
          (nil? x) xs*
          (seq? x) (concat (flat x) xs*)
          :else (cons x xs*))))))

(defn render-full [parent {tag :html/tag :keys [template] :as widget}]
  (cond
    (nil? widget) nil
    (string? widget) (.appendChild parent (.createTextNode js/document widget))
    template (render-full parent (template widget))
    tag (let [el (.createElement js/document tag)]
          (doseq [[k v] (:html/attributes widget)]
            (aset el k v))
          (if-let [text (:text widget)]
            (render-full el (str text))
            (doseq [child (-> widget :children flat)]
              (render-full el child)))
          (.appendChild parent el))
    :else (assert false (str "TODO: just recurse?" widget))))

(defn render-root [id widget]
  (let [el (.getElementById js/document id)]
    ;TODO: Better way to clear children? Also, patch diff, don't clear.
    (while (pos? (alength (.-children el)))
      (.removeChild el (.-firstChild el)))
    (render-full el widget)))
