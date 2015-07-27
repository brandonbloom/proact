(ns proact.render.browser
  (:require [cljs.pprint :refer [pprint]]
            [bbloom.vdom.core :as vdom]
            [bbloom.vdom.browser :as browser]
            [proact.render.dom :refer [tree->vdom]]
            [proact.render.expand :refer [expand-all]]))

(defn render [mounts]
  (assert (= (count mounts) 1) "TODO: implement multi-root")
  (let [[mount widget] (first mounts)]
    (-> widget
        expand-all
        (assoc :dom/mount mount)
        tree->vdom
        browser/render
        ;(select-keys [:trace #_#_ :created :destroyed])
        ;:trace (as-> x (map (comp first second) x))
        ;pprint
        )
    nil))
