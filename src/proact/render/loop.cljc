(ns proact.render.loop)

(defonce callbacks (atom {}))

(defonce timeout (atom nil))

(defn notify! []
  (doseq [[_ f] @callbacks]
    (f))
  (reset! timeout nil))

(defn trigger! []
  ;;XXX requestAnimationFrame ?
  #?(:cljs (swap! timeout #(or % (js/setTimeout notify! 0)))
     :clj (assert false "Not implemented yet"))) ;XXX

(defn add-listener [key f]
  (swap! callbacks assoc key f)
  (trigger!))

(defn remove-listener [key]
  (swap! callbacks dissoc key)
  nil)
