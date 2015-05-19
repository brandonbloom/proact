(ns ^:figwheel-always proact.core
  (:require [clojure.string :as str]
            [proact.render :refer [render-root]]
            [proact.html :as html]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(def todos [{:text "OMG"
             :completed? true}
            {:text "blah blah"
             :completed? false}])


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(defn class-names [m]
  (str/join (for [[k v] m :when v] k)))


(defn todo-item [{{:keys [completed? editing?] :as todo} :data, :as widget}]
  {:html/tag "li"
   :html/attributes {"class" (class-names {"completed" completed?
                                           "editing" editing?})}
   :children [{:html/tag "div"
               :html/attributes {"className" "view"}
               :children [{:html/tag "input"
                           ;XXX onChange
                           :html/attributes {"className" "toggle"
                                             "type" "checkbox"
                                             "checked" completed?}}
                          {:html/tag "label"
                           ;XXX onDoubleClick
                           :text (:text todo)}
                          {:html/tag "button"
                           ;XXX onClick
                           :html/attributes {"className" "destroy"}}]}
              {:html/tag "input"
               ;XXX ref editField
               ;XXX onBlur, onChange, onKeyDown
               :html/attributes {"className" "edit"
                                 ;XXX "value" this.state.editText
                                 }}]})

(defn todo-footer [{{:keys [active completed showing]} :data :as widget}]
  ;XXX use completed
  (html/footer {"id" "footer"}
    (html/span {"id" "todo-count"}
      (html/strong {} (str active)) (str showing " left"))
    (html/ul {"id" "filters"}
      (html/li {} (html/a {"href" "#/"} "All"))
      (html/li {} (html/a {"href" "#/active"} "Active"))
      (html/li {} (html/a {"href" "#/completed"} "Completed")))))

(defn app [widget]
  (let [showing :active
        items (for [{:keys [id completed?] :as todo} todos
                    :when (case showing
                            :active (not completed?)
                            :completed completed?
                            true)]
                {:template todo-item
                 :key id
                 :data todo
                 ; onToggle, onDestroy, onEdit, editing, onSave, onCancel
                 })
        completed (count (filter :completed? todos))
        active (- (count todos) completed)
        footer {:template todo-footer
                :data {:active active
                       :completed completed
                       :showing showing}
                ;XXX onClearCompleted
                }
        main (when (seq todos)
               {:html/tag "section"
                :html/attributes {"id" "main"}
                :children [{:html/tag "input"
                            :html/attributes {"id" "toggle-all"
                                              "type" "checkbox"
                                              "checked" (zero? active)}
                            ; onChange, checked
                            }
                           {:html/tag "ul"
                            :html/attributes {"id" "todo-list"}
                            :children (vec items)}]})
        input {:html/tag "input"
               :html/attributes {"id" "new-todo"
                                 "placeholder" "What needs to be done?"
                                 "autofocus" true}
               ; onKeyDown
               }]
    {:html/tag "div"
     :children [{:html/tag "header"
                 :html/attributes {"id" "header"}
                 :children [{:html/tag "h1"
                             :text "todos"}
                            input]}
                main
                footer]}))

(render-root "todoapp" (app {}))
