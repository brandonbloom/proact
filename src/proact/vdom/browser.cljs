(ns proact.vdom.browser)

(defonce state (atom {:vdom BLAH :nodes {}}))

(defmulti mutate* (fn [state [method & args]] method))

(defn mutate [op]
  (swap! state mutate* op))

(defmethod mutate* :mount [state [_ eid id]]
  (let [el (.getElementById js/document eid)]
    (assert el (str "No element with id: " eid))
    (.appendChild (get-in state [:nodes id]))
    state))

(defn- remove-child [state id]
  (.removeChild (get-in state [:nodes id])
  )

(defmethod mutate* :unmount [state [_ id]]
  (remove-child state id)
  state)

(defmethod mutate* :detatch [state [_ id]]
  (remove-child state id)
  state)

(defmethod mutate* :create-text [state [_ id text]]
  ;;XXX document.createTextNode
  state ;XXX assoc in new node
  )

(defmethod mutate* :set-text [state [_ id text]]
  (set! (.-text (get-in state [:nodes id])) text)
  state)

(defmethod mutate* :create-element [state [_ id tag]]
  state ; XXX assoc new node
  )

(defmethod mutate* :remove-props [state [_ id props]]
  ;;XXX handle recursive maps specially:
  ;;XXX clearing style sub-props: set to ''   -- necessary?
  ;;XXX clearing attributes sub-props: call removeAttribute
  state)

(defmethod mutate* :set-props [state [_ id props]]
  ;;XXX handle recursive maps specially
  ;;XXX set nested for style sub-props
  ;;XXX call setAttribute for attributes sub-props
  state)

(defmethod mutate* :insert-child [state [_ parent-id index child-id]]
  ;;XXX .insertBefore
  state)

(defmethod mutate* :free [state [_ id]]
  state ;;XXX call dispose top-down, remove nodes from map
  )
