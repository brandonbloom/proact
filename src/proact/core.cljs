(ns ^:figwheel-always proact.core
  (:require [cljs.pprint :refer [pprint]]
            [proact.render.expand :refer [expand-all]]
            [proact.render.dom :refer [tree->vdom]]
            [proact.examples.todo :as todo]
            [bbloom.vdom.core :as vdom]
            [bbloom.vdom.browser :as browser]))

(enable-console-print!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(-> todo/app
    expand-all
    tree->vdom
    (vdom/mount "todoapp" [[] :root])
    browser/render
    ;pprint
    )
