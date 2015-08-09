(ns ^:figwheel-always proact.core
  (:require [proact.examples.todo :as todo]
            [proact.widgets.tools :as tools]
            [proact.render.loop :as loop]
            [proact.render.browser :as browser]
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
        root (assoc todo/app :data {:todos @todo/state :showing showing})
        root (assoc tools/designer :data {:widget root})
        root {:dom/tag "div"
              :dom/props browser/delegates
              :dom/mount "root"
              :children [root]}]
    (browser/render root)))

(defonce watch (loop/add-listener ::watch (fn [& _] (render))))

(defn on-navigate [token]
  (reset! nav-token token)
  (render))

(defonce history
  (let [h (History.)]
    (gevents/listen h ghistory/NAVIGATE #(on-navigate (.-token %)))
    (.setEnabled h true)
    h))

(render)
