(ns proact.graph
  (:require [clojure.set :as set]))

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
           (let [k (get-in x [:attributes :key] i)
                 id (conj path [(:tag x) k])]
             (-> x
                 (update :attributes dissoc :key)
                 (assoc :id id)
                 (update :children assign-ids id))))
         (range)
         xs)))

(defn maps->nodes
  ([x] (maps->nodes {} x))
  ([nodes {:keys [id children] :as x}]
   (assert (some? id) (str "No id for node: " x))
   (assert (nil? (nodes id)) (str "Duplicate ID: " id))
   (reduce maps->nodes
           (assoc nodes id (update x :children #(mapv :id %)))
           (map #(assoc % :parent id) children))))

(def empty-vdom {:mounts {} :nodes {} :mounted #{} :detatched #{}})

(defn maps->vdom [x]
  (let [g (maps->nodes x)]
    {:mounts {}
     :nodes g
     :mounted #{}
     :detatched #{(:id x)}}))

(defn seqs->vdom [x]
  (-> x seqs->maps assign-ids maps->vdom))

;; This is a strange set of primitives to build diff out of, but they were
;; chosen to map well on to imperative DOM manipulations.

;TODO trace all of these operations

(defn mount [vdom eid id]
  (let [n (get-in vdom [:nodes id])]
    (assert n (str "Cannot mount unknown node: " id))
    (assert (nil? (:parent n)) (str "Cannot mount interior node: " id)))
  (assert (nil? (get-in vdom [:mounted id])) (str "Already mounted: " id))
  (-> vdom
      (assoc-in [:mounts eid] id)
      (assoc-in [:nodes id :mount] eid)
      (update :mounted conj id)
      (update :detatched disj id)))

(defn unmount [vdom id]
  (let [n (get-in vdom [:nodes id])]
    (assert n (str "Cannot unmount unknown node: " id))
    (assert (:mount n) (str "Node already not mounted: " id))
    (-> vdom
        (update :mounts dissoc id)
        (update-in [:nodes id] dissoc :mount)
        (update :mounted disj id)
        (update :detatched conj id))))

(defn detatch [vdom id]
  ;(let [n (get-in vdom [:nodes id])]
  ;  (assert n (str "No such node id: " id))
  ;  (assert (:parent n)
  ;(update :detatched conj id)
  vdom ;XXX
  )

(defn create-text [vdom id text]
  (assert (nil? (get-in vdom [:nodes id])) (str "Node already exists: " id))
  (-> vdom
      (assoc-in [:nodes id] {:id id :tag :text :text text})
      (update :detatched conj id)))

(defn set-text [vdom id text]
  (assert (= (get-in vdom [:nodes id :tag]) :text)
          (str "Cannot set text of non-text node: " id))
  (assoc-in vdom [:nodes id :text] text))

(defn create-element [vdom id tag]
  (assert (nil? (get-in vdom [:nodes id])) (str "Node already exists: " id))
  (-> vdom
      (assoc-in [:nodes id] {:id id :tag tag :children []})
      (update :detatched conj id)))

(defn remove-attributes [vdom id attributes]
  (assert (string? (get-in vdom [:nodes id :tag]))
          (str "Cannot remove attributes of non-element node: " id))
  (update-in vdom [:nodes id :attributes] #(reduce disj % attributes)))

(defn set-attributes [vdom id attributes]
  (assert (string? (get-in vdom [:nodes id :tag]))
          (str "Cannot set attributes of non-element node: " id))
  (update-in vdom [:nodes id :attributes] merge attributes))

(defn append-child [vdom parent-id child-id] ;XXX just use insert-child ?
  (prn "APPEND_CHILD")
  vdom)

(defn insert-child [vdom parent-id index child-id]
  (prn "INSERT_CHILD")
  vdom)

(defn free [vdom id]
  (assert (get-in vdom [:detatched id])
          (str "Cannot free non-detatched node:" id))
  ((fn rec [vdom id]
     ;;TODO trace frees (top-down or bottom-up?)
     (reduce rec
             (update vdom :nodes dissoc id)
             (get-in vdom [:nodes id :children])))
   (update vdom :detatched disj id) id))

;;;

(declare patch-children)

(defn update-text [vdom before {:keys [id text] :as after}]
  (if (= (:text before) text)
    vdom
    (set-text vdom id text)))

(defn patch-children [vdom goal id]
  (println "patch-children" (get-in goal [:nodes id]))
  vdom ;XXX
  )

(defn update-element [vdom goal before {:keys [id attributes] :as after}]
  (let [removed (set/difference (-> before :attributes keys set)
                                (-> attributes keys set))
        vdom (remove-attributes vdom id removed)
        old-attrs (:attributes before)
        updated (reduce (fn [acc [k val]]
                          (if (= (old-attrs k val) val)
                            acc
                            (assoc acc k val)))
                        nil
                        attributes)]
    (patch-children vdom goal id)))

(defn update-node [vdom goal before {:keys [id tag] :as after}]
  (println "update-node")
  (assert (= (:tag before) tag) (str "Cannot transmute node type for id " id))
  (if (= tag :text)
    (update-text vdom id before after)
    (update-element vdom goal before after)))

(defn create-node [vdom goal {:keys [id tag] :as node}]
  (println "create-node")
  (if (= tag :text)
    (create-text vdom id (:text node))
    (-> vdom
        (create-element id tag)
        (set-attributes id (:attributes node))
        (patch-children goal id))))

(defn patch-node [vdom goal id]
  (let [{:keys [tag] :as after} (get-in goal [:nodes id])]
    (if-let [before (get-in vdom [:nodes id])]
      (update-node vdom goal before after)
      (create-node vdom goal after))))

(defn patch [vdom goal]
  (let [unmounted (set/difference (:mounted vdom) (:mounted goal))
        freed (set/difference (:detatched vdom) (:detatched goal))
        vdom (reduce unmount vdom unmounted)
        vdom (reduce (fn [vdom id]
                       (patch-node vdom goal id))
                     vdom
                     (-> goal :nodes keys))
        ;XXX do mounts
        vdom (reduce free vdom freed)]
    vdom))

(defn diff [before after]
  (:trace (patch before after))) ;;XXX must implement :trace

(comment

  (-> '(div {} (span {:key "k"} "foo") (b {} "bar"))
      seqs->maps
      assign-ids
      maps->vdom
      (mount "root" [["div" 0]])
      ;(mount "root" [0 "k"])
      fipp.edn/pprint
      )

  (defn assert-patch [before after]
    (let [after* (patch before after)]
      (fipp.edn/pprint
        (if (not= after* after)
          {:before before
           :expected after
           :actual after*}
          {:before before
           :after after}))))

  (let [vdom (seqs->vdom '(div {"tabindex" 0}
                            (span {:key "k"} "foo")
                            (b {} "bar")))]
    (assert-patch empty-vdom vdom)
    ;(assert-patch vdom empty-vdom)
    )

)
