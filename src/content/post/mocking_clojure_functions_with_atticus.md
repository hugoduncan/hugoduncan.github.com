---
title: "Mocking Clojure Functions with Atticus"
pubdate: "2010-05-18"
tags: [ "lisp", "clojure", "test", "mock", "atticus" ]
Description: "I dislike most mocking, and try and avoid it as much as possible. Sometimes it is however the only realistic way of testing.  I did a quick survey of mocking tools in clojure, and found them very much reflecting the Java mocking&#10;libraries. Clojure has a few more dynamic capabilities than Java, so I thought a little about how these could be used to make a simple mocking facility, and atticus is what I came up with."
aliases: ["/post/2010/mocking_clojure_functions_with_atticus.xhtml"]
---
<p>I dislike most mocking, and try and avoid it as much as possible. Sometimes
it is however the only realistic way of testing.  I did a quick survey of
mocking tools in clojure, and found them very much reflecting the Java mocking
libraries. Clojure has a few more dynamic capabilities than Java, so I thought a
little about how these could be used to make a simple mocking facility, and <a href="http://github.com/hugoduncan/atticus">atticus</a> is what I came up
with.</p>
<p>There is a consensus that mocking should be implemented by binding a
function's var to a new function for the duration of a test, and atticus does
this too. Atticus' premise is that we can do simple mocking by declaring the
mock function as a local function definition, and have the local function do the
argument checking, return value setting, etc.  The simplest case would be
something like below, which checks the value of its argument and specifies a
return value:</p>
<pre class="clojure">
;; pull in namespaces
(use 'clojure.test)
(require 'atticus.mock)

;; define test which mocks f
(deftest mock-test
  (atticus.mock/expects
    [(f [arg]
       (is (= arg 1) "Check argument")
       arg)] ; set the return value
    ;; in a real test case this would be called
    ;; indirectly by some other function
    (is (= 1 (f 1)) "Call mocked function"))
</pre>
<p>At the moment, I have added two macros to this.  <code>once</code>, which
checks a function is called once, and <code>times</code>, which checks that a
function is called a specific number of times. The macros are used to wrap the
body of the mock function, which keeps the function's expected behaviour in one
place.</p>
<pre class="clojure">
;; define test, that should be called just once
(deftest mock-test
  (atticus.mock/expects
    [(f [arg]
       (atticus.mock/once
         (is (= arg 1) "Check argument")
         arg))]
    (is (= 1 (f 1)) "Call mocked function"))
</pre>
<pre class="clojure">
;; define test, that should be called exactly twice
(deftest mock-test
  (atticus.mock/expects
    [(f [arg]
       (atticus.mock/times 2
         (is (= arg 1) "Check argument")
         arg))]
    (is (= 1 (f 1)) "Call mocked function")
    (is (= 1 (f 1)) "Call mocked function"))
</pre>
<p>So what do you think, is this a reasonable approach? Not having the explicit
calls to <code>returns</code>, etc, might be seen as a loss of declarative
clarity, but I for one prefer this, as it gives you the full power of the
language to test the arguments and set the return value.</p>
<h3>References</h3>
<ul>
<li><a href="http://s-expressions.com/2010/01/24/conjure-simple-mocking-and-stubbing-for-clojure-unit-tests/">conjure – simple mocking and stubbing for Clojure unit-tests</a></li>
<li><a href="http://richhickey.github.com/clojure-contrib/mock-api.html">clojure.contrib.mock</a></li>
<li><a href="http://code.google.com/p/test-expect/">test-expect</a></li>
<li><a href="http://blog.n01se.net/?p=134">Using binding to mock out even “direct linked” functions in Clojure</a></li>
</ul>
