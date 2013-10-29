---
title: "Generating Source Files with Leiningen"
Pubdate: "2013-10-28"
tags: [ "leiningen", "clojure" ]
Description: "Generating source files with the leiningen run task.  Adds project
              specific source generation to prep-tasks."
draft: true
---

Recently, we needed to include some generated source files in a
project.  The source code generation was project specific, so we
didn't want to have to create a leiningen plugin specifically for it.
To get this to work required using quite a few of
[leiningen's](https://github.com/technomancy/leiningen) features.

The source code generator is going to live in the `my.src-generator`
namespace.  Here's an example, that just generates a namespace
declaration for the `my.gen` namespace under
`target/generated/my/gen.clj`.

```clj
(ns my.src-generator
  (:require [clojure.java.io :refer [file]]))

(defn generate []
  (doto (file "target" "generated" "my" "gen.clj")
    (-> #(.getParentFile) #(.mkdirs))
    (spit "(ns my.gen)")))
```

The source generation code should not be packaged in the jar, so we
place it in `dev-src/my/src_generator.clj`, and add `dev-src` and the
generated source directories to the `:dev` profile's `:source-paths`.
The `:dev` profile is automatically used by leiningen unless it is
producing a jar file.

```clj
:profiles {:dev {:source-paths ["src" "dev-src" "target/generated"]}}
```

To use lein's `run` task we need to add a `-main` function to the
`my.src-generator` namespace.

```clj
(defn -main [& args]
  (generate))
```

In the `project.clj` file we also tell lein about the main namespace.
In order to avoid AOT compilation of the main namespace, we mark it
with `:skip-aot` metadata.

```clj
:main ^:skip-aot my.src-generator
```

The generated files need to end up in the jar (and possibly be compiled),
so we put them on the `:source-paths` in the project.

```clj
:source-paths ["src" "target/generated"]
```

Now we can tell lein to generate the source files whenever we use the
project.  We do this by adding the `run` task to the `:prep-tasks`
key.  The tricky bit here is that the `run` task will itself invoke
the `:prep-tasks`, so we want to make sure we don't end up calling the
task recursively and generating a stack overflow.  To solve this, add
a `gen` profile, and disable the prep tasks in it.  We use the
`:replace` metadata to ensure this definition takes precedence.  See
the
[leiningen profile documentation](https://github.com/technomancy/leiningen/blob/master/doc/PROFILES.md#merging)
for more information on `:replace` and it's sibling `:displace`.

```clj
:gen {:prep-tasks ^:replace []}
```

Then use this profile when setting the `:prep-tasks` key in the project.

```clj
:prep-tasks [["with-profile" "+gen,+dev" "run"]  "compile"]
```

Now when we run any command, the sources are generated.

Finally we may want to just invoke the source generation, so let's
create an alias to make `lein gen` run the generator.  We need the
`gen` profile for this, or otherwise the generator will run twice.

```clj
:aliases {"gen" ["with-profile" "+gen,+dev" "run"]}
```

For reference, the final project.clj looks like this:

```clj
(defproject my-proj "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :source-paths ["src" "target/generated"]
  :main ^:skip-aot my.src-generator
  :prep-tasks [["with-profile" "+gen,+dev" "run"]  "compile"]
  :profiles {:dev {:source-paths ["src" "dev-src" "target/generated"]}
             :gen {:prep-tasks ^:replace []}}
  :aliases {"gen" ["with-profile" "+gen,+dev" "run"]})
```

This required using many of lein's features to get working - hopefully
you'll find a use for some of them.
