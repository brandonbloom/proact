(ns proact.html
  #?(:cljs (:use-macros [proact.html :only [defelements]])))

#?(:clj
(defmacro defelement [name]
  `(defn ~name [~'props & ~'children]
     {:html/tag ~(str name)
      :html/props ~'props
      :children ~'children})))

#?(:clj
(defmacro defelements [& names]
  `(do ~@(map #(list `defelement %) names))))

(defelements
  div span a strong b ul ol li header footer section h1 h2 h3 h4 h5 h6
  input label button
  )
