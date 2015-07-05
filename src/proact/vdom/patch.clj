(ns proact.vdom.patch
  (:require [clojure.set :as set]
            [proact.vdom.core :as vdom]
            [proact.vdom.trace :refer [traced]]))

(defn update-text [vdom before {:keys [id text] :as after}]
  (if (= (:text before) text)
    vdom
    (vdom/set-text vdom id text)))

(defn detatch-last-child [vdom id]
  (vdom/detatch vdom (-> (vdom/node vdom id) :children peek)))

(defn update-element [vdom before {:keys [id attributes] :as after}]
  (let [removed (set/difference (-> before :attributes keys set)
                                (-> attributes keys set))
        vdom (vdom/remove-attributes vdom id removed)
        old-attrs (:attributes before)
        updated (reduce (fn [acc [k val]]
                          (if (= (old-attrs k val) val)
                            acc
                            (assoc acc k val)))
                        nil
                        attributes)]
    (vdom/set-attributes vdom id updated)))

(defn update-node [vdom before {:keys [id tag] :as after}]
  (assert (= (:tag before) tag) (str "Cannot transmute node type for id " id))
  (if (= tag :text)
    (update-text vdom id before after)
    (update-element vdom before after)))

(defn create-node [vdom {:keys [id tag] :as node}]
  (if (= tag :text)
    (vdom/create-text vdom id (:text node))
    (-> vdom
        (vdom/create-element id tag)
        (vdom/set-attributes id (:attributes node)))))

(defn patch-node [vdom {:keys [id tag] :as node}]
  (if-let [before (vdom/node vdom id)]
    (update-node vdom before node)
    (create-node vdom node)))

(def ^:dynamic *parented*) ;XXX debug-only

(defn patch-children [vdom {:keys [id children]}]
  (let [;; Move desired children in to place.
        vdom (reduce (fn [vdom [i child]]
                       (assert (nil? (*parented* child))
                               (str "Duplicate node id: " child))
                       (set! *parented* (conj *parented* child))
                       (if (= (get-in (vdom/node vdom id) [:children i]) child)
                         vdom
                         (vdom/insert-child vdom id i child)))
             vdom
             (map vector (range) children))
        ;; Detatch any leftover trailing children.
        n (max 0 (- (count (:children (vdom/node vdom id)))
                    (count children)))
        vdom (nth (iterate (fn [vdom]
                             (detatch-last-child vdom id))
                           vdom)
                  n)]
    vdom))

(defn patch [vdom goal]
  (let [N0 (vdom/nodes vdom), M0 (vdom/mounts vdom), H0 (vdom/hosts vdom)
        N1 (vdom/nodes goal), M1 (vdom/mounts goal), H1 (vdom/hosts goal)
        unmounted (->> (remove (fn [[eid nid]] (= (M1 eid) nid)) M0)
                       (map second))
        mounted (remove (fn [[nid eid]] (= (H0 nid) eid)) H1)
        freed (set/difference (set (map :id N0)) (set (map :id N1)))
        vdom (reduce vdom/unmount vdom unmounted)
        vdom (reduce patch-node vdom N1)
        els (filter #(-> % :tag string?) N1)
        vdom (binding [*parented* #{}]
               (reduce patch-children vdom els))
        freed (remove #(:parent (vdom/node vdom %)) freed)
        vdom (reduce vdom/free vdom freed)
        vdom (reduce (fn [vdom [nid eid]]
                          (vdom/mount vdom eid nid))
                        vdom
                        mounted)]
    vdom))

(defn diff [before after]
  (-> (traced before) (patch after) :trace))

(comment

  (defn assert-patch [before after]
    (let [after* (patch before after)]
      (fipp.edn/pprint
        (if (not= after* after)
          {:before before
           :expected after
           :actual after*}
          {:before before
           :after after}))))

  (defn party [before after]
    (assert-patch before after)
    (fipp.edn/pprint (diff before after))
    )

  (require '[proact.vdom.syntax :refer [seqs->vdom]])
  (require '[proact.vdom.trace :refer [traced]])
  (let [vdom (seqs->vdom '(div {"tabindex" 0}
                            (span {:key "k"} "foo")
                            (b {} "bar")))
        vdom (vdom/mount vdom "blah" [["div" 0]])
        ]
    ;(party vdom/null vdom)
    (party vdom vdom/null)
    )

)
