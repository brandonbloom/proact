/*jshint quotmark:false */
/*jshint white:false */
/*jshint trailing:false */
/*jshint newcap:false */
/*global React, Router*/
var app = app || {};

(function () {
  'use strict';

  app.ALL_TODOS = 'all';
  app.ACTIVE_TODOS = 'active';
  app.COMPLETED_TODOS = 'completed';
  var TodoFooter = app.TodoFooter;
  var TodoItem = app.TodoItem;

  var ENTER_KEY = 13;

  var TodoApp = React.createClass({
    getInitialState: function () {
      return {
        nowShowing: app.ALL_TODOS,
        editing: null
      };
    },

    componentDidMount: function () {
      var setState = this.setState;
      var router = Router({
        '/': setState.bind(this, {nowShowing: app.ALL_TODOS}),
        '/active': setState.bind(this, {nowShowing: app.ACTIVE_TODOS}),
        '/completed': setState.bind(this, {nowShowing: app.COMPLETED_TODOS})
      });
      router.init('/');
    },

    handleNewTodoKeyDown: function (event) {
      if (event.which !== ENTER_KEY) {
        return;
      }

      event.preventDefault();

      var val = this.refs.newField.getDOMNode().value.trim();

      if (val) {
        this.props.model.addTodo(val);
        this.refs.newField.getDOMNode().value = '';
      }
    },

    toggleAll: function (event) {
      var checked = event.target.checked;
      this.props.model.toggleAll(checked);
    },

    toggle: function (todoToToggle) {
      this.props.model.toggle(todoToToggle);
    },

    destroy: function (todo) {
      this.props.model.destroy(todo);
    },

    edit: function (todo) {
      this.setState({editing: todo.id});
    },

    save: function (todoToSave, text) {
      this.props.model.save(todoToSave, text);
      this.setState({editing: null});
    },

    cancel: function () {
      this.setState({editing: null});
    },

    clearCompleted: function () {
      this.props.model.clearCompleted();
    },

  });

  var model = new app.TodoModel('react-todos');

  function render() {
    React.render(
      <TodoApp model={model}/>,
      document.getElementById('todoapp')
    );
  }

  model.subscribe(render);
  render();
})();
