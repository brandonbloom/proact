(ns proact.widgets.controls
  (:require
    #?(:clj [clojure.core.match :refer [match]])
    #?(:cljs [cljs.core.match :refer-macros [match]])
    [proact.render.state :as state]))

(defn button-handler [widget e]
  (match [e]
    [[:click]] (:command widget)
    :else e))

(def button {:handler button-handler
             :command [:press]})

(defn toggle-handler [widget e]
  (match [e]
    [[:click]] (state/put! (:id widget)
                           {:value (not (get-in widget [:state :value]))})
    :else e))

(def toggle {:state {:value false}
             :handler toggle-handler
             :children []})

;TODO expander
