(ns proact.html-macros)

(defmacro defelement [name]
  `(defn ~name [~'attrs & ~'children]
     {:html/tag ~(str name)
      :html/attributes ~'attrs
      :children ~'children}))

(defmacro defelements [& names]
  `(do ~@(map #(list `defelement %) names)))
