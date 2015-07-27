(ns proact.render.browser
  (:require [cljs.pprint :refer [pprint]]
            [bbloom.vdom.core :as vdom]
            [bbloom.vdom.browser :as browser]
            [proact.render.dom :refer [tree->vdom]]
            [proact.render.expand :refer [expand-all]]))

;;XXX Right now this is the expanded *tree*, but should be the graph.
(defonce global (atom nil))

(defn render [mounts]
  (assert (= (count mounts) 1) "TODO: implement multi-root")
  (let [[mount widget] (first mounts)
        expanded (expand-all widget)]
    (reset! global expanded)
    (-> expanded
        (assoc :dom/mount mount)
        tree->vdom
        browser/render
        ;(select-keys [:trace #_#_ :created :destroyed])
        ;:trace (as-> x (map (comp first second) x))
        ;pprint
        )
    nil))

(defn path-to [node]
  (let [id (browser/identify node)]
    ;;XXX Right now this is a linear search, but should be
    ;;XXX a hash lookup plus walking parent references.
    ((fn rec [path widget]
       (let [path (conj path widget)]
         (if (= (:id widget) id)
           path
           (->> widget
                :children
                (map #(rec path %))
                (filter some?)
                first))))
     [] @global)))

(defn translate [event]
  (case (.-type event)
    "click" :click))

(defn route-event [event]
  (let [target (.-target event)
        path (path-to target)
        translated (translate event)
        e (reduce (fn [e widget]
                    (if-let [handler (:handler widget)]
                      (handler e)
                      e))
                  translated
                  path)]
    (when (not= e translated)
      (.stopPropagation event)
      (.preventDefault event))
    e))
