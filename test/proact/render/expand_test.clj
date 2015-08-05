(ns proact.render.expand-test
  (:require [clojure.test :refer :all]
            [proact.render.expand :refer [expand-all]]))

(comment

  (->
    "abc"
    expand-all
    fipp.edn/pprint
    )

)

(deftest expand-test
  (are [in out] (= (expand-all in) out)

    "abc"
    {:dom/tag :text
     :text "abc"
     :children []
     :id [[] :root]}

    {:children [{:dom/tag "img"
                 :dom/props {"src" "http://example.com/img.jpg"}}
                {:dom/tag :text
                 :text "A Thing"}]}
    {:children [{:dom/tag "img",
                 :dom/props {"src" "http://example.com/img.jpg"},
                 :children [],
                 :child-index 0,
                 :scope [:root],
                 :id [[:root] 0]}
                {:dom/tag :text,
                 :text "A Thing",
                 :children [],
                 :child-index 1,
                 :scope [:root],
                 :id [[:root] 1]}]
     :id [[] :root]}

    {:data {:first-name "Brandon"
            :last-name "Bloom"}
     :template (fn [{{:keys [first-name last-name]} :data}]
                 {:dom/tag :text
                  :text (str last-name ", " first-name)})}
    {:data {:first-name "Brandon", :last-name "Bloom"},
            :children [{:dom/tag :text,
            :text "Bloom, Brandon",
            :id [[] :root],
            :children []}]}

    ))
