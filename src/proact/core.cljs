(ns ^:figwheel-always proact.core
  (:require [cljs.pprint :refer [pprint]]
            [proact.render.expand :refer [expand-all]]
            [proact.render.dom :refer [tree->vdom]]
            [proact.examples.todo :as todo]
            [bbloom.vdom.core :as vdom]
            [bbloom.vdom.browser :as browser]))

(enable-console-print!)

(-> todo/app
    expand-all
    tree->vdom
    (vdom/mount "todoapp" [[] :root])
    browser/render
    ;pprint
    )
