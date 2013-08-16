---
title: "How to Build Clojurescript Libs with JavaScript Dependencies"
Pubdate: "2013-08-16"
tags: [ "clojurescript", "javascript", "packaging" ]
description: "A summary of different strategies for packaging JavaScipt dependencies in a Clojurescript library"
---

Using JavaScript dependencies in a Clojurescript library seems to be hard.  It
took me many hours to understand how it should work.  A big thanks to
[Chas Emerick][cemerick] for setting me straight on most of this.

Luke Vanderhart [posted][lukespost] a general introduction to using Javascript
libraries in Clojurescript.  Go read it if you haven't already - this post
assumes you have.

While that post is an excellent description of using JavaScript in
a Clojurescript application, it doesn't really address JavaScript in
Clojurescript libraries, which has the additional problem of how to ensure the
JavaScript dependency is available in the consumer of the library.  A
Clojurescript library should definitely be capable of providing it's
dependencies, but should also allow the consumer to override the version of
these dependencies.

## Don't package the JavaScript

The first approach is to simply not provide the JavaScript at all.  This is the
approach taken by [jayq][jayq] for example.  The consumer of jayq, or any
library that uses jayq, is required to provide jQuery through the JavaScript
runtime.  This can take the form of a `&lt;script&gt;` link in the browser page,
or a call to `webPage#injectJs` in phantomJS.  The compile `:libs` or
`:foreign-libs` options can not be used to provide the dependency, as there is
no way for the compiler to know that jayq depends on the namespace provided by
these options.

For the consumer of the library to use compiler`:optimizations` other than
`:whitespace`, they will need to provide an `:externs` file.

## Package JavaScript

The second approach is to package the JavaScript via a Clojurescript namespace.
This involves adding a `require` on a namespace to the code that directly
depends on the JavaScript, and arranging for that Clojurescript namespace to
load the JavaScript, using either of the compiler`:libs` or `:foreign-libs`
options.

The Clojurescript library can make the JavaScript library available in its
resources.  The library consumer can then use resource via the `:libs` or
`:foreign-libs` options, depending on whether or not the JavaScript contains a
`goog.provides` call.

If the library is packaged with a `goog.provides` call, then the consumer can
not replace the version using `:libs [""]` - the use of an explicit prefix in
`:libs` is needed to prevent more than one JavaScript implementation trying to
provide the clojure namespace, or the use of `:foreign-libs` where the namespace
is explicitly mapped.

For examples, the [pprng][pprng] library packages its dependency with a
`goog.provides` call, allowing the use of `:libs [""]` to pull in the
dependency.  The [papadom][papadom] library on the other hand provides vanilla
javascript dependencies, and requires the use of the more verbose
`:foreign-libs` option.

If the JavaScript is to be provided in the runtime, then the consumer will have
to provide an empty namespace definition to satisfy the require in the
Clojurescript library, and the`:externs` file as in the first case.

## Postscript

There are several assumptions in much of the documentation that I didn't see
explicitly explained.  I'll record these here for posterity.

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


[lukespost]: http://lukevanderhart.com/2011/09/30/using-javascript-and-clojurescript.html "Luke Vanderhart's post on JavaScript libs"

[jayq]: https://github.com/ibdknox/jayq "jayq"

[cemerick]: http://cemerick.com "Chas Emerick"

[pprng]: https://github.com/cemerick/pprng "pprng"
[papadom]: https://github.com/hugoduncan/papadom "papadom"
