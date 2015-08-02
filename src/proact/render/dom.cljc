(ns proact.render.dom
  (:require [bbloom.vdom.core :as vdom]))

(defn append-child [vdom pid cid]
  (let [idx (-> vdom (vdom/node pid) :children count)]
    (vdom/insert-child vdom pid idx cid)))

(defn tree->vdom [widget]
  ((fn rec [vdom parent {:keys [id children]
                         tag :dom/tag, mount :dom/mount
                         :as widget}]
     (when tag
       (assert id))
     (let [vdom (cond
                  (= tag :text) (vdom/create-text vdom id (:text widget))
                  tag (-> vdom
                          (vdom/create-element id tag)
                          (vdom/set-props id (:dom/props widget)))
                  :else vdom)
           vdom (cond-> vdom
                  (and tag parent) (append-child parent id)
                  mount (vdom/mount mount id))
           parent (if tag id parent)]
       (reduce #(rec %1 parent %2)
               vdom
               children)))
   vdom/null nil widget))

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
