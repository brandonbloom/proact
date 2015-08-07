(ns ^:figwheel-always proact.core
  (:require [proact.examples.todo :as todo]
            [proact.widgets.tools :as tools]
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
        ;root {:dom/tag "div"
        ;      :dom/mount "tools"
        ;      :children [(assoc tools/tree-view
        ;                        :item {:foo [:a :b] :bar "c"})]}
        ]
    (browser/render {"todoapp" root})))

;;XXX set flag & timeout to re-render *just once*
(defonce watch (add-watch todo/state ::render (fn [& _] (render))))

(defn on-navigate [token]
  (reset! nav-token token)
  (render))

(defonce history
  (let [h (History.)]
    (gevents/listen h ghistory/NAVIGATE #(on-navigate (.-token %)))
    (.setEnabled h true)
    h))

(render)
