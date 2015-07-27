(ns proact.examples.todo
  (:require #?(:cljs [proact.render.browser :as browser])
            [proact.html :as html]
            [proact.html-util :refer [classes link-to]]))


(def mock-todos
  [{:id "todo-1"
    :text "OMG"
    :completed? true}
   {:id "todo-2"
    :text "it works!"
    :completed? false}])

;;; Event Handlers

(defn on-destroy-click [e]
  #?(:cljs
      (prn (browser/path-to (.-target e)))))

;;; Views

(def todo-item
  {:template
   (fn [{{:keys [completed? editing?] :as todo} :data}]
     (html/li {"className" (classes {"completed" completed?
                                     "editing" editing?})}
       (html/div {"className" "view"}
         (html/input {"className" "toggle"
                      "type" "checkbox"
                      ;XXX onChange
                      "checked" completed?})
         (html/label {} (:text todo)) ;XXX onDoubleClick
         (html/button {"className" "destroy"
                       "onclick" on-destroy-click}))
       ;;XXX ref editField
       (html/input {"className" "edit"
                          ;XXX "value" this.state.editText
                          ;XXX onBlur, onChange, onKeyDown
                          })))})

;; Fn syntax is more convenient, but loses some benefits of components...
(defn filter-link [showing k href content]
  (html/li {}
    (html/a {"className" (when (= showing k) "selected")
             "href" href}
      content)))

(def todo-footer
  {:data {:active 2 :completed 5 :showing :all}
   :template
   (fn [{{:keys [active completed showing]} :data}]
     (html/footer {"id" "footer"}
       (html/span {"id" "todo-count"}
         (html/strong {} (str active))
         (if (= active 1) " item left" " items left"))
       (html/ul {"id" "filters"}
         (filter-link showing :all "#/" "All")
         (filter-link showing :active "#/active" "Active")
         (filter-link showing :completed "#/completed" "Completed"))
       (when (pos? completed)
         (html/button {"id" "clear-completed"} ;XXX onClick
           "Clear completed"))))})

(def app
  {:data {:todos mock-todos :showing :all}
   :template
   (fn [{{:keys [todos showing]} :data}]
     (let [items (for [{:keys [id completed?] :as todo} todos
                       :when (case showing
                               :active (not completed?)
                               :completed completed?
                               true)]
                   ;; onToggle, onDestroy, onEdit, editing, onSave, onCancel
                   (assoc todo-item :key id :data todo))
           completed (count (filter :completed? todos))
           active (- (count todos) completed)
           ;XXX footer onClearCompleted
           footer (assoc todo-footer :data {:active active
                                            :completed completed
                                            :showing showing})
           main (when (seq todos)
                  (html/section {"id" "main"}
                    (html/input {"id" "toggle-all"
                                 "type" "checkbox"
                                 ;;XXX onChange, checked
                                 "checked" (zero? active)})
                    (html/ul {"id" "todo-list"} items)))
           input (html/input {"id" "new-todo"
                              "placeholder" "What needs to be done?"
                              "autofocus" true})] ;XXX onKeyDown
       (html/div {}
         (html/header {"id" "header"}
           (html/h1 {} "todos")
           input)
         main
         footer)))})
