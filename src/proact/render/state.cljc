(ns proact.render.state
  (:refer-clojure :exclude [get]))

(defonce global (atom {}))

(defn get [id]
  (@global id))

(defn put! [id state]
  (swap! global update id merge state)
  nil)

(defn clear! [id]
  (swap! global dissoc id)
  nil)
