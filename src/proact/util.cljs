(ns proact.util)

(defn flat [xs]
  (lazy-seq
    (when-first [x xs]
      (let [xs* (flat (next xs))]
        (cond
          (nil? x) xs*
          (seq? x) (concat (flat x) xs*)
          :else (cons x xs*))))))
