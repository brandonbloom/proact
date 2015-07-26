(ns proact.render.dom
  (:require [bbloom.vdom.core :as vdom]))

(defn tree->nodes
  ([x] (tree->nodes nil {} x))
  ([parent nodes {:keys [id children], tag :html/tag, :as x}]
   (assert (nil? (nodes id)) (str "duplicate id: " id))
   (let [node (when tag
                (cond-> {:tag tag :props (:html/props x) :parent parent}
                  (string? tag) (assoc :children (mapv :id children))))
         parent (if node id parent)]
     (reduce (partial tree->nodes parent)
             (if node (assoc nodes id node) nodes)
             children))))

(defn tree->vdom [x]
  {:post [(vdom/valid? %)]}
  (let [g (tree->nodes x)]
    (assoc vdom/null :nodes g :detached #{(:id x)})))

(comment

  (require 'proact.examples.todo)
  (require 'proact.render.expand)
  (->
    (proact.examples.todo/app {})
    proact.render.expand/expand-all
    tree->vdom
    fipp.edn/pprint
    )

)
