---
title: "Clojure Evaluate and Formatting in Emacs Markdown Buffers"
Pubdate: "2013-08-26"
tags: [ "emacs", "markdown", "nrepl", "clojure" ]
Description: "When editing Clojure blocks in mardown documents, allow formating and evaluation of code blocks with clojure-mode."
---

When writing documentation or blog posts about Clojure code, it is
very useful to be able to format Clojure code blocks using
[`clojure-mode`][clojure-mode] and evaluate code with [`nrepl.el`][nrepl].

This can be enabled using [`multi-web-mode`][multi-web-mode], which
allows a single buffer to use different major modes for different
sections of the buffer (and is not limited to just web modes).
Install `multi-web-mode` using `M-x package-install multi-web-mode`,
or using `M-x el-get-install multi-web-mode` from the excellent
[`el-get`][el-get], or by checking the project from github and
installing manually.

To configure this for clojure and markdown, you'll
need something like this in your `init.el` or `.emacs` file.

```lisp
(require 'multi-web-mode)
(setq mweb-default-major-mode 'markdown-mode)
(setq mweb-tags '((clojure-mode "^```clj$" "^```$")))
(setq mweb-filename-extensions '("md" "markdown"))
(multi-web-global-mode 1)
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
`clojure-mode` buffer, and the code will display exactly as in amy `.clj`
file.

This technique is not limited to clojure and markdown, but could be
made to work whenever you would like differing major modes in distinct
parts of your Emacs buffers.  You can adjust `mweb-tags`
appropriately, adding as many major modes as you need.  The regions
for each major mode are detected using regular expressions.

[nrepl]: https://github.com/clojure-emacs/nrepl.el "nrepl.el"
[clojure-mode]: https://github.com/clojure-emacs/clojure-mode "clojure-mode"
[el-get]: http://tapoueh.org/emacs/el-get.html "el-get"
[multi-web-mode]: https://github.com/fgallina/multi-web-mode "multi-web-mode"
