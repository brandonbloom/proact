(ns proact.widgets.tools
  (:require [proact.html :as html] ;XXX
            [proact.widgets.layout :as layout]))

(def designer
  )

(declare tree-view)

(def entry-view
  {:template
   (fn [{[k v] :item}]
     {:prototype layout/row
      :children
      [(html/div {"style" {"font-weight" "bold"
                           "padding-left" "5px"
                           "border-left" "2px solid red"}}
         (pr-str k))
       (assoc tree-view :item v)]})})

(def map-view
  {:template
   (fn [{:keys [item]}]
     {:prototype layout/column
      :children
      [{:item-prototype entry-view
        :items (vec item)}]})})

(def vector-view
  {:template
   (fn [{:keys [item]}]
     {:prototype layout/column
      :children
      [{:item-prototype entry-view
        :items (mapv vector (range) item)}]})})

(def scalar-view
  {:template
   (fn [{:keys [item]}]
     (html/div {"style" {"font-style" "italic"}}
       (pr-str item)))})

(def tree-view
  {:template
   (fn [{:keys [item]}]
     {:dom/tag "div"
      :dom/props {"style" {"padding" "2px 8px"}}
      :children
      [(assoc (cond
                (vector? item) vector-view
                (map? item) map-view
                :else scalar-view)
              :item item)]})})
