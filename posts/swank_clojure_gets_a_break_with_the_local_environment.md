
Recently I got fed up with a couple of warts in swank-clojure, so I made a couple of small fixes, and that lead to a couple of new features.  Using SLIME with Clojure has never been as smooth as using it with Common Lisp, and the lack of debug functionality beyond the display of stack traces is particularly onerous.  Recently, George Jahad and Alex Osborne's <a href="http://github.com/GeorgeJahad/debug-repl">debug-repl</a> showed the possibility of adding a break macro to enter the debugger with the call stack intact and local variables visible.  This functionality is now in swank-clojure.

Consider the following example, adapted from debug-repl:

<pre>
  (let [c 1
        d 2]
    (defn a [b c]
      (swank.core/break)
      d))
  (a "foo" "bar")
</pre>

Running this now brings up the following SLDB debug frame:

<pre>
BREAK:
  [Thrown class java.lang.Exception]

Restarts:
 0: [QUIT] Quit to the SLIME top level
 1: [CONTINUE] Continue from breakpoint

Backtrace:
  0: user$eval__1666$a__1667.invoke(NO_SOURCE_FILE:1)
  1: user$eval__1670.invoke(NO_SOURCE_FILE:1)
  2: clojure.lang.Compiler.eval(Compiler.java:5358)
  3: clojure.lang.Compiler.eval(Compiler.java:5326)
  4: clojure.core$eval__4157.invoke(core.clj:2139)
  --more--
</pre>

<p>As you can see, the stack trace reflects the location of the breakpoint, and there is a CONTINUE restart. Pressing "1", or Enter on the CONTINUE line, or clicking the CONTINUE line should all cause the the debugger frame to close, and the result of the function call, 2, to be displayed in the REPL frame.</p>

<p>Enter, or "t", on the first line of the stacktrace causes the local variables to be displayed:</p>
<pre>
BREAK:
  [Thrown class java.lang.Exception]

Restarts:
 0: [QUIT] Quit to the SLIME top level
 1: [CONTINUE] Continue from breakpoint

Backtrace:
  0: user$eval__1666$a__1667.invoke(NO_SOURCE_FILE:1)
      Locals:
        b = foo
        d = 2
        c = bar
  1: user$eval__1670.invoke(NO_SOURCE_FILE:1)
  2: clojure.lang.Compiler.eval(Compiler.java:5358)
  3: clojure.lang.Compiler.eval(Compiler.java:5326)
  4: clojure.core$eval__4157.invoke(core.clj:2139)
  --more--
</pre>

<p>Pressing enter on one of the local variable lines will pull up the SLIME inspector with that value. If you go back to the REPL without closing the SLDB frame, there will be no prompt, but pressing enter should give you one.  The local variables are then all be avaiable for evaluation form the REPL.</p>

<p>Should an error occur while you are using the REPL, you will be placed in a nested debug session, with an "ABORT" restart to return to the previous debug level.</p>

<p>Finally, restarts are now displayed for each of the exceptions in the exception cause chain.</p>

<pre>
  (let [c 1
        d 2]
    (defn a [b c]
      (throw (Exception. "top" (Exception. "nested" (Exception. "bottom"))))
      d))
  (a "foo" "bar")
</pre>

<p>This will bring up the debugger with 2 cause restarts, which can be used to examine the related stack traces.</p>

<pre>
top
   [Thrown class java.lang.Exception]

Restarts:
  0: [QUIT] Quit to the SLIME top level
  1: [CAUSE1] Invoke debugger on cause  nested [Thrown class java.lang.Exception]
  2: [CAUSE2] Invoke debugger on cause   bottom [Thrown class java.lang.Exception]

Backtrace:
   0: user$eval__1752$a__1753.invoke(NO_SOURCE_FILE:1)
   1: user$eval__1756.invoke(NO_SOURCE_FILE:1)
   2: clojure.lang.Compiler.eval(Compiler.java:5358)
   3: clojure.lang.Compiler.eval(Compiler.java:5326)
   4: clojure.core$eval__4157.invoke(core.clj:2139)
  --more--
</pre>

<p>The break functionality is known only to work from the REPL thread at the moment.  With that small proviso, I hope you enjoy the new functionality - at least it provides a basic debug functionality until full JPDA/JDI integration is tackled.</p>