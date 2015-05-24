(ns ^:figwheel-always proact.core
  (:require [cljs.pprint :refer [pprint]]
            [proact.render :refer [render]]
            [proact.examples.todo :as todo]))

(enable-console-print!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

;(render {"todoapp" (todo/app {})})

(let [id (str (gensym))
      el (.createElement js/document "div")]
  (println id)
  (aset el "id" id)
  (.. js/document -body (appendChild el))
  (render {id {:text "hello"}}))

(pprint (:graph @proact.render/state))
