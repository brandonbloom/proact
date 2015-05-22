(ns proact.examples.todo
  (:require [proact.html :as html]
            [proact.html-util :refer [classes link-to]]))


(def todos [{:text "OMG"
             :completed? true}
            {:text "blah blah"
             :completed? false}])



(defn todo-item [{{:keys [completed? editing?] :as todo} :data, :as widget}]
  (html/li {"className" (classes {"completed" completed?
                                  "editing" editing?})}
    (html/div {"className" "view"}
      (html/input {"className" "toggle"
                   "type" "checkbox"
                   ;XXX onChange
                   "checked" completed?})
      (html/label {} (:text todo)) ;XXX onDoubleClick
      (html/button {"className" "destroy"})) ;XXX onClick
    ;;XXX ref editField
    (html/input {"className" "edit"
                       ;XXX "value" this.state.editText
                       ;XXX onBlur, onChange, onKeyDown
                       })))

(defn todo-footer [{{:keys [active completed showing]} :data :as widget}]
  ;XXX use completed
  (html/footer {"id" "footer"}
    (html/span {"id" "todo-count"}
      (html/strong {} (str active)) (str showing " left"))
    (html/ul {"id" "filters"}
      (html/li {} (link-to "#/" "All"))
      (html/li {} (link-to "#/active" "Active"))
      (html/li {} (link-to "#/completed" "Completed")))))

(defn app [widget]
  (let [showing :all ;XXX state
        items (for [{:keys [id completed?] :as todo} todos
                    :when (case showing
                            :active (not completed?)
                            :completed completed?
                            true)]
                {:template todo-item
                 :key id
                 ;; onToggle, onDestroy, onEdit, editing, onSave, onCancel
                 :data todo})
        completed (count (filter :completed? todos))
        active (- (count todos) completed)
        footer {:template todo-footer
                ;XXX onClearCompleted
                :data {:active active
                       :completed completed
                       :showing showing}}
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
      footer)))
