(ns ^:figwheel-always proact.core
  (:require [cljs.pprint :refer [pprint]]
            [proact.render.expand :refer [expand-all]]
            [proact.render.dom :refer [tree->vdom]]
            [proact.examples.todo :as todo]
            [bbloom.vdom.core :as vdom]
            [bbloom.vdom.browser :as browser]
            [goog]
            [goog.events :as gevents]
            [goog.history.EventType :as ghistory])
  (:import [goog History]))

(enable-console-print!)


(defonce nav-token (atom nil))

(defn render []
  (let [showing (case @nav-token
                  "/active" :active
                  "/completed" :completed
                  :all)
        root (assoc-in todo/app [:data :showing] showing)]
    (-> root
        expand-all
        tree->vdom
        (vdom/mount "todoapp" [[] :root])
        browser/render
        ;pprint
        )))

(defn on-navigate [token]
  (reset! nav-token token)
  (render))

(defonce history
  (let [h (History.)]
    (gevents/listen h ghistory/NAVIGATE #(on-navigate (.-token %)))
    (.setEnabled h true)
    h))

(render)
