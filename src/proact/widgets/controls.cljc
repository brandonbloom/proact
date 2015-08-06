(ns proact.widgets.controls
  (:require
    #?(:clj [clojure.core.match :refer [match]])
    #?(:cljs [cljs.core.match :refer-macros [match]])))

(defn button-handler [widget e]
  (match [e]
    [[:click]] (:command widget)
    :else e))

(def button {:handler button-handler
             :command [:press]})

;TODO toggle

;TODO expander
