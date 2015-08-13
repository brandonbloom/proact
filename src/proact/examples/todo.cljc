(ns proact.examples.todo
  (:require
    #?(:clj [clojure.core.match :refer [match]])
    #?(:cljs [cljs.core.match :refer-macros [match]])
    [proact.render.loop :as loop]
    [proact.render.state :as state]
    [proact.widgets.controls :as ctrl]
    #?(:cljs [proact.render.browser :as browser])
    [proact.html :as html]
    [proact.html-util :refer [classes link-to]]))

;;; Model

(def mock-todos
  [{:id "todo-1"
    :text "OMG"
    :completed? true}
   {:id "todo-2"
    :text "it works!"
    :completed? false}])

(defonce state
  (add-watch (atom mock-todos) ::watch (fn [& _] (loop/trigger!))))

(defn add-todo [todos text]
  (conj todos {:id (str (gensym "todo_"))
               :text text
               :completed? false}))

(defn destroy-todo [todos id]
  (vec (remove #(= (:id %) id) todos)))

(defn clear-completed [todos]
  (vec (remove :completed? todos)))

(defn set-completed [todos id value]
  (mapv (fn [todo]
          (if (= (:id todo) id)
            (assoc todo :completed? value)
            todo))
        todos))

;;; Event Handlers

(defn raise! [& args]
  (apply swap! state args)
  nil)

(defn app-handler [widget e]
  (match [e]
    [[:todo/destroy-todo id]] (raise! destroy-todo id)
    [[:todo/clear-completed]] (raise! clear-completed)
    [[:todo/set-completed id value]] (raise! set-completed id value)
    [[:todo/add-todo text]] (raise! add-todo text)
    [[:todo/edit id]] (state/put! (:id widget) {:editing id})
    :else e))

(defn new-handler [_ e]
  (match [e]
    [[:key-down 13]] [:todo/add-todo "omg"] ;XXX need text from widget somehow
    :else e))

(defn todo-handler [widget e]
  (case (first e)
    :change [:todo/set-completed (-> widget :item :id) (:checked? (second e))]
    :double-click [:todo/edit (-> widget :item :id)]
    e))

;;; Views

(def todo-item
  ;; onToggle, onDestroy, onEdit, editing, onSave, onCancel
  {:handler todo-handler
   :template
   (fn [{{:keys [completed? editing?] :as todo} :item}]
     (html/li {"className" (classes {"completed" completed?
                                     "editing" editing?})}
       (html/div {"className" "view"}
         (html/input {"className" "toggle"
                      "type" "checkbox"
                      ;XXX onChange
                      "checked" completed?})
         (html/label {} (:text todo)) ;XXX onDoubleClick
         (assoc (html/button {"className" "destroy"})
                :prototype ctrl/button
                :command [:todo/destroy-todo (:id todo)]))
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
         (assoc (html/button {"id" "clear-completed"}
                             "Clear completed")
                :prototype ctrl/button
                :command [:todo/clear-completed]))))})

(def app
  {:data {:todos mock-todos :showing :all}
   :state {:editing nil}
   :handler app-handler
   :template
   (fn [{{:keys [todos showing]} :data
         {:keys [editing]} :state}]
     (let [completed (count (filter :completed? todos))
           active (- (count todos) completed)
           footer (assoc todo-footer :data {:active active
                                            :completed completed
                                            :showing showing})
           todos (map #(assoc % :editing? (= (:id %) editing)) todos)
           main (when (seq todos)
                  (html/section {"id" "main"}
                    (html/input {"id" "toggle-all"
                                 "type" "checkbox"
                                 ;;XXX onChange, checked
                                 "checked" (zero? active)})
                    (assoc (html/ul {"id" "todo-list"})
                           :item-prototype todo-item
                           :item-filter (fn [{:keys [completed?]}]
                                          (case showing
                                            :active (not completed?)
                                            :completed completed?
                                            true))
                           :items todos)))
           input (assoc (html/input {"id" "new-todo"
                                     "placeholder" "What needs to be done?"
                                     "autofocus" true})
                        :handler new-handler)]
       (html/div {"id" "todoapp"}
         (html/header {"id" "header"}
           (html/h1 {} "todos")
           input)
         main
         footer)))})
