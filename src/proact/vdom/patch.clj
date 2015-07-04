(ns proact.vdom.patch
  (:require [clojure.set :as set]
            [proact.vdom.core :as vdom]))

(defn update-text [vdom before {:keys [id text] :as after}]
  (if (= (:text before) text)
    vdom
    (vdom/set-text vdom id text)))

(defn detatch-last-child [vdom id]
  (vdom/detatch vdom (peek (get-in vdom [:nodes id :children]))))

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
  (if-let [before (get-in vdom [:nodes id])]
    (update-node vdom before node)
    (create-node vdom node)))

(def ^:dynamic *parented*) ;XXX debug-only

(defn patch-children [vdom {:keys [id children]}]
  (let [;; Move desired children in to place.
        vdom (reduce (fn [vdom [i child]]
                       (assert (nil? (*parented* child))
                               (str "Duplicate node id: " child))
                       (set! *parented* (conj *parented* child))
                       (if (= (get-in vdom [:nodes id :children i]) child)
                         vdom
                         (vdom/insert-child vdom id i child)))
             vdom
             (map vector (range) children))
        ;; Detatch any leftover trailing children.
        n (max 0 (- (count (get-in vdom [:nodes id :children]))
                    (count children)))
        vdom (nth (iterate (fn [vdom]
                             (detatch-last-child vdom id))
                           vdom)
                  n)]
    vdom))

(defn patch [vdom goal]
  (let [unmounted (set/difference (:mounted vdom) (:mounted goal))
        ids (-> goal :nodes keys)
        nodes (map #(get-in goal [:nodes %]) ids)
        freed (set/difference (-> vdom :nodes keys set) ids)
        vdom (reduce vdom/unmount vdom unmounted)
        vdom (reduce patch-node vdom nodes)
        el-ids (filter #(string? (get-in vdom [:nodes % :tag])) ids)
        els (map #(get-in goal [:nodes %]) el-ids)
        vdom (binding [*parented* #{}]
               (reduce patch-children vdom els))
        ;XXX do mounts
        freed (remove #(get-in vdom [:nodes % :parent]) freed)
        vdom (reduce vdom/free vdom freed)]
    vdom))

(defn diff [before after]
  (:trace (patch before after))) ;;XXX must implement :trace

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

  (require '[proact.vdom.syntax :refer [seqs->vdom]])
  (let [vdom (seqs->vdom '(div {"tabindex" 0}
                            (span {:key "k"} "foo")
                            (b {} "bar")))]
    (assert-patch vdom/null vdom)
    (assert-patch vdom vdom/null)
    )

)
