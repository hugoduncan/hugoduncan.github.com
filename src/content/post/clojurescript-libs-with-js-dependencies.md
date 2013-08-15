---
title: "How to build clojurescript libs with javascript dependencies"
Pubdate: "2013-08-13"
tags: [ "clojurescript", "javascript", "packaging" ]
draft: true
---
# Build Clojurescript Libs with Javascript Dependencies

Using JavaScript dependencies in a Clojurescript library seems to be hard.  It
took me hours to understand how it should work.

Luke Vanderhart [posted][lukespost] a general introduction to using Javascript
libraries in Clojurescript.  Go read it if you haven't already - this post
assumes you have.

While this is an excellent description of using JavaScript in
a Clojurescript application, it doesn't really address JS in Clojurescript
libraries, which has the additional problem of how to ensure the JS dependency
is available in the consumer of the library.  A Clojurescript library should
definitely be capable of providing it's dependencies, but should also allow the
consumer to override the version of these dependencies.

There are several assumptions in much of the documentation that should be made
explicit, which I'll try to do now.

A clojurescript library is always a source code library.  There is no such thing
as the linking of compiled clojurescript artifacts.

Neither`:libs` nor`:foreign-libs` actually changes how the JavaScript is
accessed within the clojurescript code.  If you include jQuery via a `:libs`,
and a `require`, you still access it through `js/jQuery`.  The `require` of the
namespace specified by `goog.provide`, or the namespace specified in the
`:foreign-libs`' `:provides` key, simply ensures the JavaScript is loaded.

The choice of compiler `:optimizations` affects what information you need to
provide, and this differs depending on whether you are providing javascript
libraries through the runtime (e.g. $lt;script&gt; tags in the browser), or
through `:libs` or `:foreign-libs` compiler options.  The simplest here is to
use the compiler options.  When providing the JavaScript via the runtime, then
everything should also just work if you are using no optimisation, or
just `:whitespace`, but as soon as you try anything else, you will need to
provide an :externs definition for the JavaScript libraries.







;; Building a Clojurescript library that depends on a JavaScript library creates





I've read this several times now, but
have still
spent hours trying to get `:foreign-libs` to work when packaging a clojurescript
library.





[lukespost]: http://lukevanderhart.com/2011/09/30/using-javascript-and-clojurescript.html "Luke Vanderhart's post on JavaScript libs"
