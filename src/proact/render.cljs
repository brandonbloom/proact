(ns proact.render
  (:require [cljs.pprint :refer [pprint]] ;XXX
            [proact.util :refer [flat]]))

(defonce state (atom {:roots {} :graph {}}))

(def ^:dynamic *before*)
(def ^:dynamic *after*)
(def ^:dynamic *moved*)
(def ^:dynamic *detatched*)
(def ^:dynamic *path*)
(def ^:dynamic *parent*)
(def ^:dynamic *index*)

(declare render-widget)

(defn get-node []
  (aget (.-childNodes *parent*) *index*))

(defn detatch []
  (println "detatch")
  (let [node (get-node)]
    (set! *detatched* (conj *detatched* node))
    (.removeChild *parent* node)
    nil))

(defn insert [inserted]
  (println "insert")
  (let [neighbor (get-node)]
    (set! *detatched* (disj *detatched* inserted))
    (set! *moved* (conj *moved* inserted))
    (if neighbor
      (.insertBefore *parent* inserted neighbor)
      (.appendChild *parent* inserted))
    inserted))

(defn substitute [replacement]
  (println "substitute")
  (let [existing (get-node)]
    (set! *detatched* (-> *detatched* (conj existing) (disj replacement)))
    (set! *moved* (conj *moved* replacement))
    (.replaceChild existing replacement)
    replacement))

(defn create-text [{:keys [text]}]
  (println "create-text")
  (.createTextNode js/document text))

(defn patch-text [before after]
  (println "patch-text")
  (if (string? before)
    (let [node (get-node)]
      (.replaceData node 0 (.-length node) (:text after))
      node)
    (substitute (create-text after))))

(defn create-children [parent children]
  (println "create-children")
  (binding [*parent* parent
            *index* 0]
    (doseq [child children]
      (.appendChild parent (render-widget child))
      (set! *index* (inc *index*)))))

(defn begin-children [widget]
  (set! *after* (assoc-in *after* [(:id widget) :child-nodes] [])))

(defn create-element [widget]
  (println "create-element" (:html/tag widget) (count (:children widget)))
  (let [el (.createElement js/document (:html/tag widget))]
    (begin-children widget)
    (doseq [[k v] (:html/attributes widget)]
      (aset el k v))
    (create-children el (:children widget))
    el))

(defn patch-children [before after]
  (println "patch-children")
  ;;TODO compare against before's children
  (begin-children after)
  (doseq [child (:children after)]
    (insert (render-widget child))
    (set! *index* (inc *index*)))
  (let [nodes (.-childNodes *parent*)]
    (while (> (.-length nodes) *index*)
      (detatch))))

(defn patch-element [before after]
  (println "patch-element")
  (if (= (:html/tag before) (:html/tag after))
    (do
      ;;TODO patch attributes
      (patch-children before after)
      (get-node))
    (substitute (create-element after))))

(defn put [{:keys [id] :as widget} create patch]
  (println "put" id)
  (let [node (if-let [existing (*before* id)]
               (do (assert (not (*moved* id)))
                   (if (= existing widget)
                     (get-node)
                     (patch existing widget)))
               (create widget))]
    (set! *after* (update-in *after* [id :child-nodes] conj node))
    node))

(defn put-text [widget]
  (println "put-text")
  (put widget create-text patch-text))

(defn put-element [widget]
  (println "put-element")
  (put widget create-element patch-element))

(defn normalize
  ([widget]
   (normalize [:proact/auto] widget))
  ([subpath widget]
   (cond
     (nil? widget) (recur subpath {})
     (string? widget)
     ,,(recur subpath {:text widget})
     (and (:html/tag widget) (:text widget))
     ,,(recur subpath (-> widget
                          (dissoc :text)
                          (assoc :children [(str (:text widget))])))
     :else
     ,,(let [key (:key widget subpath)
             id (:id widget {:context *path* :key key})
             children (-> widget :children flat)
             children (for [[i child] (map vector (range) children)
                            :let [subpath (conj subpath [:proact/child i])]]
                        (normalize subpath child))]
         (-> widget (dissoc :key) (assoc :id id :children (vec children)))))))

(defn expand [widget]
  (let [{:keys [template] :as widget} (normalize widget)]
    (if template
      (recur (template widget))
      widget)))

(defn render-widget [widget]
  (println "render")
  (let [expanded (expand widget)]
    (cond
      (:text expanded) (put-text expanded)
      (:html/tag expanded) (put-element expanded)
      :else (assert false (str "TODO: just recurse to children?" expanded)))))

(defn render-root [id widget]
  (binding [*parent* (.getElementById js/document id)
            *index* 0
            *path* [id]]
    (patch-children nil ;XXX get from root state
                    {:id [:proact/root id] :children [widget]})))

(defn release [node]
  ;;TODO recursive cleanup, skip sub-trees in *moved*
  (println node " was detatched"))

;;; BEGIN Public Interface

(defn render [roots]
  (println "-----------------")
  (let [{old-roots :roots, :keys [graph]} @state]
    (binding [*before* graph
              *after* graph
              *moved* #{}
              *detatched* #{}]
      (doseq [[id widget] roots]
        (render-root id widget))
      ;;XXX detatch roots removed from old-roots to roots
      (doseq [node *detatched*]
        (release node))
      (swap! state assoc :roots roots :graph *after*))))
