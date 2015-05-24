(ns ^:figwheel-always proact.core
  (:require [proact.render :refer [render]]
            [proact.examples.todo :as todo]))

(enable-console-print!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(render {"todoapp" (todo/app {})})
