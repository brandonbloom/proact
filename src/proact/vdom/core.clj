(ns proact.vdom.core
  (:require [clojure.core.rrb-vector :as rrb]))

;;; Vector Utilities.

(defn remove-at [v i]
  (rrb/catvec (rrb/subvec v 0 i) (rrb/subvec v (inc i))))

(defn remove-item [^clojure.lang.APersistentVector v x]
  (remove-at v (.indexOf v x)))

(defn insert [v i x]
  (rrb/catvec (rrb/subvec v 0 i) [x] (rrb/subvec v i)))

;; This is a strange set of primitives to build diff out of, but they were
;; chosen to be atomic and map well on to imperative DOM manipulations.

(def null {:mounts {} :nodes {} :mounted #{} :detatched #{}})

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
  (let [{:keys [parent] :as n} (get-in vdom [:nodes id])]
    (assert n (str "No such node id: " id))
    (assert parent (str "No already detatched: " id))
    (-> vdom
        (update-in vdom [:nodes parent :children] remove-item id)
        (update-in vdom [:nodes id] dissoc :parent)
        (update vdom :detatched conj id))))

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

(defn insert-child [vdom parent-id index child-id]
  (let [n (get-in vdom [:nodes child-id])
        vdom (if-let [p (:parent n)]
               (update-in vdom [:nodes p :children] remove-item child-id)
               vdom)]
    (-> vdom
        (assoc-in [:nodes child-id :parent] parent-id)
        (update-in [:nodes parent-id :children] insert index child-id)
        (update-in [:detatched] disj child-id))))

(defn free [vdom id]
  (assert (get-in vdom [:detatched id])
          (str "Cannot free non-detatched node:" id))
  ((fn rec [vdom id]
     ;;TODO trace frees (top-down or bottom-up? or as an atomic set?)
     (reduce rec
             (update vdom :nodes dissoc id)
             (get-in vdom [:nodes id :children])))
   (update vdom :detatched disj id) id))
