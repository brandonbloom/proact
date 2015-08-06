(ns proact.widgets.layout)

(def flex
  {:dom/tag "div"
   :dom/props {"style" {"display" "flex"}}})

(def row (assoc-in flex [:dom/props "style" "flex-direction"] "row"))
(def column (assoc-in flex [:dom/props "style" "flex-direction"] "column"))
