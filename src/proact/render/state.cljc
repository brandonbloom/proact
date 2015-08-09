(ns proact.render.state
  (:refer-clojure :exclude [get])
  (:require [proact.render.loop :as loop]))

(defonce global
  (add-watch (atom {}) ::watch (fn [& _] (loop/trigger!))))

(defn get [id]
  (@global id))

(def left-merge (partial merge-with (fn [x _] x)))

(defn init! [id state]
  ((swap! global update id left-merge state) id))

(defn put! [id state]
  (swap! global update id merge state)
  nil)

(defn clear! [id]
  (swap! global dissoc id)
  nil)
