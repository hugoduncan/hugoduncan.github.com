---
title: "Exploring a todo app with core.async"
Pubdate: "2013-08-11"
tags: [ "clojurescript", "core.async", "templating" ]
---

We're going to build an equivalent of the [AngularJS TODO example][angulartodo]
using core.async, and a templating library, [papadom][papadom], that I've
written to help in this.

[Clojurescript][clojurescript] recently gained a [CSP][csp] implemetation via
[core.async][core.async], similar to [Go's channels][golangchannel], or
[CML's channels][cmlchannel] (CML also has a nice select).  Bruce Haumann
started exploring this with [ClojureScript Core.Async Todos][brucetodo], and
David Nolen has been looking at how to use core.async for
[responsive design][cspresponsive].  In this post, we'll take the TODO example,
and take it a little further.

## Basic Display

We'll start with just displaying a list of todo items.  For this we'll need a
template, so we'll just write this in HTML, and add a `t-template` attribute,
which enables us to use mustache style templating of values to display.  This
doesn't use mustache sections for looping, in order to preserve valid HTML
markup.

```html
<h1>TODOS</h1>
<ul class="unstyled">
  <li t-template="todos">{{text}}</li>
</ul>
```

To get this to show something we'll need some code:

```clj
(ns app
  (:require
   [papadom.template :refer [compile-templates render]]))

(defn start []
  (compile-templates)
  (render {:todos [{:text "learn papadom" :done false}
                   {:text "write a papadom app" :done false}]}))
```

When you call `app.start()` from the page containing the above template, you'll
see a list of two todo entries.

## Adding an event

Now we have something displayed, lets add a checkbox to mark todo items as done:

```html
<ul class="unstyled">
  <li t-template="todos">
    <input type="checkbox" t-prop="done" t-event="done"
           t-id="index" index="{{@index}}">
    <span>{{text}}</span>
  </li>
</ul>
```

The `t-prop` attribute tells the template to which data value to display as the
checkbox.

The `t-event` attribute specifies that we want an event.  When the checkbox is
clicked, we will get a core.async message with a `:done` event type.  We need to
know which todo was clicked, so we use the `t-id` attribute to list the
attributes whose values should be sent as the event data -- in this case the
index, which has a value based on handlebars style `@index` property.

Now we need some code to process the events.  To do this we'll define an
`app` function that will be passed a state atom containing a map with our todos
state, and a core.async channel from which to read events.  The function will
loop over events, and dispatch them as required.

```clj
(defn app
  [state event-chan]
  (go
   (loop [[event event-data] (<! event-chan)]
     (case event
       :done
       (let [nv (boolean (:checked event-data))]
         (swap! state assoc-in [:todos (:index event-data) :done] nv)))
     (recur (<! event-chan)))))
```

When the `app` function receives a `:done` event, it will update the state atom
appropriately.  Now we have our state updating, we'll need to display it, which
we can again do with the `render` function.

```clj
(defn show-state [state]
  (render "state" state))
```

We still need to get `show-state` called, and we'll arrange this in a modified
`start` function.  This will create an atom for the state, and add a watch on
the atom that will call `show-state`.

```clj
(defn start
  []
  (let [event-chan (chan)
        state (atom nil)]
    (compile-templates)
    (template-events event-chan)
    (add-watch state :state (fn [key ref old new] (show-state new)))
    (reset! state {:todos [{:text "Learn papadom" :done false}
                           {:text "Build a papadom app" :done false}]})
    (app state event-chan))))
```

We've also added a core.async channel, `event-chan`, which we've passed to
`template-events` to arrange delivery of the events defined in our template.  We
pass this channel to the `app` function to start processing the events.

This shows the basic structure of the application.

## Adding New Todo Elements

To allow you to add new todo items, we'll add a form to our template,
specifying a `t-event` attribute, which will cause an event to be sent when the
form is submitted, with the form's input values as the event data.

```html
<form t-event="add-todo">
  <input type="text" t-prop="text" size="30" placeholder="add new todo here">
  <input class="btn btn-primary" type="submit" value="add">
</form>
```

To process this new event, we'll add a case to the `app` function loop's `case`
form.

```clj
:add-todo
(swap! state update-in [:todos]
       conj {:text (:text (input-seq->map event-data))
             :done false})
```

This uses the `input-seq->map` helper to convert the data from the form into a
map, and we extract the `:text` value (defined by `t-prop` in the `input`
element).

And we're done.  To see a full working example have a look at the
[template][todotemplate] and [code][todocode] in the todo example of papadom.
To run the example:

```shell
git clone https://github.com/hugoduncan/papadom.git
cd papadom/examples/todo
lein ring server
```


[angulartodo]: http://angularjs.org/#add-some-control "Angular TODO example"

[brucetodo]: http://rigsomelight.com/2013/07/18/clojurescript-core-async-todos.html "Bruce Haumann's 'ClojureScript Core.Async Todos'"

[clojurescript]: https://github.com/clojure/clojurescript "Clojurescript"

[cmlchannel]: http://www.cs.cornell.edu/Courses/cs312/2006fa/recitations/rec24.html "CML Channels"

[core.async]:  http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html "Clojure core.async Channels"

[csp]: http://en.wikipedia.org/wiki/Communicating_sequential_processes "Communicating Sequential Processes"

[cspresponsive]: http://swannodette.github.io/2013/07/31/extracting-processes/ "David Nolan's 'CSP is Responsive Design'"

[golangchannel]: http://golang.org/ref/spec#Channel_types "Go Channels"


[kemingweather]: http://keminglabs.com/blog/angular-cljs-weather-app/ "Kevin Lynagh's 'Building an iOS weather app with Angular and ClojureScript'"


[purnam]: https://github.com/zcaudate/purnam "purnam"
[clang]: https://github.com/pangloss/clang/ "clang"

[todotemplate]: https://github.com/hugoduncan/papadom/blob/master/examples/todo/resources/public/index.html "TODO HTML Template"
[todocode]: https://github.com/hugoduncan/papadom/blob/master/examples/todo/src/papadom/example/todo.cljs "TODO Clojurescript Code"


[papadom]: https://github.com/hugoduncan/papadom "Papadom templating library"
