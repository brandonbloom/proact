(ns proact.html-util
  (:require [clojure.string :as str]
            [proact.html :as html]))

(defn classes [m]
  (str/join " " (for [[k v] m :when v] k)))

(defn link-to [href & children]
  (apply html/a {"href" href} children))
