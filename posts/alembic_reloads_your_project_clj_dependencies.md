
You're working away in a Clojure REPL, when you realise you need to
add a dependency.  You add the dependency to your [leiningen][lein]
`project.clj` file and then?  Instead of shutting down your REPL,
loosing whatever state you have built up, you can use
[Alembic][alembic] to load the new dependencies.  Simply call
`(alembic.still/load-project)`.

Of course, it still has to work within the confines of the JVM's
classloaders, so you can only add dependencies, and not modify
versions or remove dependencies, but this should still cover a lot of
use cases.

To use alembic on a single project, simply add it as a dependency in
your `:dev` profile in `project.clj`:

```clj
:profiles {:dev {:dependencies [[alembic "0.2.0"]]}}
```

To make alembic available in all your projects, and it to the `:user`
profile in `~/.lein/profiles.clj` instead:

```clj
{:user {:dependencies [[alembic "0.2.0"]]}}
```

Alembic also allows you to directly add dependencies without editing
your `project.clj` file, using the `distill` function.  Use this if you
are just exploring libraries, for example.

Finally a big thank you to [Anthony Grimes][raynes] and the other
[flatland][flatland] developers for removing classlojure's dependency
on `useful`, which should make this all much more robust.

[alembic]: https://github.com/pallet/alembic#alembic "Alembic"
[lein]: http://leiningen.org "Leiningen"
[raynes]: http://blog.raynes.me/ "Raynes"
[flatland]: https://github.com/flatland/ "flatland"