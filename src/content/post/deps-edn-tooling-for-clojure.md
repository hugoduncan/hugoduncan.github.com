---
title: "deps.edn Tooling for Clojure"
date: 2020-08-10T21:47:47-04:00
draft: true
---

Coming back to clojure after a few years in Python land, I would like to
update some of my project configurations.  The "new" `deps.edn` format
for specifying project dependencies is fantastic for what it covers -
local project development.  Once you want to publish your project you'll
find it doesn't support a lot of what Leiningen or Boot can do.

## Current tools for working with deps.edn

There seem to be many independent projects that have sprung up to fill
the gaps.

Some of these are focused, like
[depster](https://github.com/healthfinch/depstar "depster tool for
building jars") or
[pack.alpha](https://github.com/juxt/pack.alpha#uploading-to-clojars-or-maven
"pack.alpha tool for building jars").

Other `deps.edn` tools are more general, for example
[revolt](https://github.com/mbuczko/revolt "revolt tool with plugins").

So far, unless I have missed the mark, there doesn't seem to be much
consensus around the best approach.

This leaves projects, such as
[orchard](https://github.com/clojure-emacs/orchard "orchard, a build
tool library"), resorting to `make` or other "build" tools to
co-ordinate the developer experience.

## How to improve on this?

What makes `deps.edn` appealing compared to `lein` or `boot`?  Firstly I
like its simplicity - it is plain EDN, and secondly it doesn't have any
tool specific configuration.  It provides a "policy" and doesn't specify
the mechanisms.  It's use of aliases is completely "opt-in" -- if you
don't need aliases, then you don't have to use them.

How about tools like `make`?  they rely on the [Unix
philosophy](https://en.wikipedia.org/wiki/Unix_philosophy), in
particular combining individual tools.  They are however language
agnostic, which misses some opportunities for deeper language
integration.

How can we learn from this and build more general tooling following this
lead?  I think there is scope for a `project.edn` file, to specify
declarative information about a project, and configuration for the tools
the project uses to manage itself.

At its simplest, it could look like this:

```clojure
{:version {:major 1 :minor 0 : :patch 0}
 :group-id "hugoduncan.org"
 :artifact-id "my-project"
}
```


We then need a tool to implement the mechanism of running the tooling.
There could be multiple tools that use this format, but let's call ours
makejack, invoked as `mj`.  Instead of writing a `Makefile`, we could
write a `mj.edn` file.

```clojure
{:targets {:jar {:tool mk-depster}}}
```

And we can invoke this with:

```shell
mj jar
```




## Implementation

The addition of [babashka](https://github.com/borkdude/babashka
"babaska, A Clojure babushka for the grey areas of Bash") to the clojure
ecosystem seems like it would provide the perfect implementation
language.

I would make this file use [aero](https://github.com/juxt/aero "A
small library for explicit, intentful configuration").

```clojure
{:version {:major 1 :minor 0 : :patch 0}
 :group-id "hugoduncan.org"
 :artifact-id "my-project"

 :full-aliases  ["test.check"]
 :codox
 {:aliases #ref [:full-aliases]
  :config
  {:source-paths              ["src"]
   :output-path               "doc"
   :src-dir-uri               "https://github.com/hugoduncan/my-project/blob/develop"
   :src-linenum-anchor-prefix "L"}}
}
```
