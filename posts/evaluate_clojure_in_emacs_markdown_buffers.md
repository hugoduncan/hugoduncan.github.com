
When writing documentation or blog posts about Clojure code, it is
very useful to be able to format Clojure code blocks using
[`clojure-mode`][clojure-mode] and evaluate code with [`nrepl.el`][nrepl].

This can be enabled using [`mmm-mode`][mmm-mode], which
allows a single buffer to use different major modes for different
sections of the buffer (and is not limited to just web modes).
Install `mmm-mode` using `M-x package-install mmm-mode`,
or using `M-x el-get-install mmm-mode` from the excellent
[`el-get`][el-get], or by checking the project from github and
installing manually.

To configure this for clojure and markdown, add this in your `init.el`
or `.emacs` file.

```lisp
(require 'mmm-auto)
(mmm-add-classes
 '((markdown-clojure
    :submode clojure-mode
    :face mmm-declaration-submode-face
    :front "^```clj[\n\r]+"
    :back "^```$")))

(setq mmm-global-mode 'maybe)
(mmm-add-mode-ext-class 'markdown-mode nil 'markdown-clojure)
```

After evaluating the above, or restarting emacs, you can test
multi-mode support by opening a markdown document, or creating a new
one, and adding a clojure source block, e.g.:

<pre>
```clj
(defn my-fn [x]
  (inc x))

(my-fn 1)
```
</pre>

Inside the code block you can format and evaluate your code as in any
`clojure-mode` buffer, and the code will display exactly as in a
`.clj` file.  By default the evaluation uses a running inferior lisp
process, which you must start yourself.  To use a running
[nrepl][nrepl] session instead, use `M-x nrepl-interaction-mode`
inside the code block.

## Using with AsciiDoc

This technique is not limited to clojure and markdown, but could be
made to work whenever you would like differing major modes in distinct
parts of your Emacs buffers.  You can add class to `mmm-mode`
appropriately, for as many major mode combinations as you need.  The
regions for each major mode are detected using regular expressions (or
by some function).

For example, if you're writing asciidoc, you might use:

```lisp
(mmm-add-classes
 '((asciidoc-clojure
    :submode clojure
    :face mmm-declaration-submode-face
    :front "\\[source, clojure\\][\n\r]+----[\n\r]+"
    :back "^----$")))
(mmm-add-mode-ext-class 'adoc-mode nil 'asciidoc-clojure)
(mmm-add-mode-ext-class 'doc-mode nil 'asciidoc-clojure)
```

## Summary

`mmm-mode` allows you to flexibly use multiple major modes in
different parts of a single emacs buffer.  Here we have shown how to
use it for `clojure-mode` code blocks in markdown or asciidoc, but it
is in no way limited to this, and it allows some fine grained
customisation to the appearance and behaviour of each major mode
block. I'm sure you'll find your own uses for `mmm-mode`.

[dirlocals]: http://www.gnu.org/software/emacs/manual/html_node/emacs/Directory-Variables.html "Emacs Directory variales"
[nrepl]: https://github.com/clojure-emacs/nrepl.el "nrepl.el"
[clojure-mode]: https://github.com/clojure-emacs/clojure-mode "clojure-mode"
[el-get]: http://tapoueh.org/emacs/el-get.html "el-get"
[mmm-mode]: https://github.com/purcell/mmm-mode/ "mmm-mode"