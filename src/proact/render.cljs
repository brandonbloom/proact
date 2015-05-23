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

(def ^:dynamic *parent*)
(def ^:dynamic *index*)

(declare render)

(defn get-node []
  (aget (.-childNodes *parent*) *index*))

(defn remove-node []
  ;;TODO track this
  (.removeChild (get-node)))

(defn replace-node [node]
  ;;TODO track this
  (.replaceChild (get-node) node))

(defn move-mode [node]
  ;;TODO track this
  (.insertBefore *parent* node (get-node)))

(defn create-text [text]
  (.createTextNode js/document text))

(defn patch-text [before after]
  (if (string? before)
    (let [node (get-node)]
      (.replaceData node 0 (.-length node) after)
      node)
    (replace-node (create-text after))))

(defn create-element [widget]
  (let [el (.createElement js/document (:html/tag widget))]
    (doseq [[k v] (:html/attributes widget)]
      (aset el k v))
    (binding [*parent* el
              *index* 0]
      (doseq [child (-> widget :children flat)]
        (.appendChild el (render child))
        (set! *index* (inc *index*))))
    el))

(defn patch-element [before after]
  (if (= (:html/tag before) (:html/tag after))
    (do
      ;;TODO patch attributes
      ;;TODO draw each child, calling move-node for each
      ;;TODO keep track of all moved & removed nodes
      (let [nodes (.-childNodes *parent*)
            n 10000] ;XXX after's number of dom children
        (while (> (.-length nodes) n)
          (remove-node))))
    (replace-node (create-element after))))

(defn put [widget create patch]
  (if-let [existing nil] ;TODO lookup by id & check not-moved
    (patch existing widget) ;XXX use *index*, do insertBefore, handle moves
    (create widget)))

(defn put-text [text]
  (put text create-text patch-text))

(defn put-element [widget]
  (put widget create-element patch-element))

(defn render [{tag :html/tag :keys [template text] :as widget}]
  (cond
    (nil? widget) nil
    (string? widget) (put-text widget)
    template (recur (template widget))
    text (recur (-> widget (dissoc :text) (assoc :children [(str text)])))
    tag (put-element widget)
    :else (assert false (str "TODO: just recurse?" widget))))

(defn render-root [id widget]
  (binding [*parent* (.getElementById js/document id)
            *index* 0]
    (while (pos? (alength (.-childNodes *parent*)))    ;XXX patch
      (.removeChild *parent* (.-firstChild *parent*))) ;XXX patch
    (.appendChild *parent* (render widget)) ;TODO: figure out how to leverage put-element
    ;;TODO process moved/removed nodes
    ))
