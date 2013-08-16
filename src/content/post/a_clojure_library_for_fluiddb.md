---
title: "A Clojure library for FluidDB"
Pubdate: "2009-09-13"
tags: [ "clojure", "fluiddb", "lisp", "advogato"]
Description: "I have released Criterium, a new project for benchmarking code in Clojure.  I found Brent Broyer's article on Java benchmarking which explains many of the pitfalls of benchmarking on the JVM, and Criterion, a benchmarking library in Haskell."
aliases: ["/post/2009/a_clojure_library_for_fluiddb.xhtml"]
---
<p><a href="http://fluidinfo.com/">FluidDB</a>, a "cloud" based triple-store, where the objects are immutable and can be tagged by anyone, launched about a month ago. As a another step to getting up to speed with <a href="http://clojure.org">Clojure</a>, I decided to write a client library, and <a href="http://github.com/hugoduncan/clj-fluiddb">clj-fluiddb</a> was born.  The code was very simple, especially as I could base the library on <a href="http://github.com/hdurer/cl-fluiddb">cl-fluiddb</a>, a Common-Lisp library.</p>
<p>I have some ideas I want to try out using FluidDB.  It's permission system is one of it's <a href="http://abouttag.blogspot.com/2009/09/permissions-worth-getting-excited-about.html">best features</a>, together with the ability to <a href="http://www.xavierllora.net/2009/08/25/liquid-rdf-meandering-in-fluiddb/">use it for RDF like triples</a> means that it could provide a usable basis for growing the semantic web.  My ideas are less grandiose, but might take as long to develop, we'll see...</p>