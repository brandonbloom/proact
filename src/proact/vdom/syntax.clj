(ns proact.vdom.syntax
  "Parse an Edn tree in to a vdom graph."
  (:require [proact.vdom.core :as vdom]))

(declare seqs->maps)

(defn seq->map [[tag attributes & children]]
  {:tag (str tag)
   :attributes attributes
   :children (mapv seqs->maps children)})

(defn seqs->maps [x]
  (cond
    (string? x) {:tag :text :text x}
    (seq? x) (seq->map x)
    :else (throw (ex-info "Invalid dom syntax" {:val x}))))

(defn assign-ids
  "Simple ID scheme of visual-tree paths. Each path element is a pair of
  the node type and either the index within the parent or an explicit key."
  ([x]
   (first (assign-ids [x] [])))
  ([xs path]
   (mapv (fn [i x]
           (let [tag (:tag x)
                 k (get-in x [:attributes :key] i)
                 id (conj path [tag k])
                 x (assoc x :id id)]
             (if (string? tag)
               (-> x
                   (update :attributes dissoc :key)
                   (update :children assign-ids id))
               x)))
         (range)
         xs)))

(defn maps->nodes
  ([x] (maps->nodes {} x))
  ([nodes {:keys [id children] :as x}]
   (assert (some? id) (str "No id for node: " x))
   (assert (nil? (nodes id)) (str "Duplicate ID: " id))
   (reduce maps->nodes
           (assoc nodes id
                  (if (-> x :tag string?)
                    (update x :children #(mapv :id %))
                    x))
           (map #(assoc % :parent id) children))))

(defn maps->vdom [x]
  (let [g (maps->nodes x)]
    {:mounts {}
     :nodes g
     :mounted #{}
     :detatched #{(:id x)}}))

(defn seqs->vdom [x]
  (-> x seqs->maps assign-ids maps->vdom))

(comment

  (require '[proact.vdom.core :as vdom])

  (-> '(div {"tabindex" 0}
         (span {:key "k"} "foo")
         (b {} "bar"))
      seqs->maps
      assign-ids
      maps->vdom
      (vdom/mount "root" [["div" 0]])
      ;(mount "root" [0 "k"])
      fipp.edn/pprint
      )

)
