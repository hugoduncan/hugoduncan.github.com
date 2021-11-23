> He allowed himself to be swayed by his conviction that human beings
> are not born once and for all on the day their mothers give birth to
> them, but that life obliges them over and over again to give birth to
> themselves.”  ― Gabriel García Márquez, Love in the Time of Cholera


*Edit*

This is a re-write - the original is below.  It now aims to provide some
recommendations on how to release for consumption by a git dependency.
It all seems very obvious in retrospect

Firstly only publish a single artifact from a single git repository, or
several artifacts with identical versions if it is a monorepo.  You can
imagine schemes that would work with multiple artifacts with independent
versions, but tooling is going to have a hard time with any such scheme.
This was the piece of the puzzle I was missing in my original post.

Once we've agreed to the above, then it becomes simple - versions become
monotonically increasing, and easy for tooling to deal with.

Just put a git tag on a release in the same way you would a version
published to maven.  For a good scheme see
[Golang Modules](https://golang.org/doc/modules/version-numbers).  Make
sure the scheme you choose sorts well with
[version-clj](https://github.com/xsc/version-clj) (h/t
[@borkdude](https://twitter.com/borkdude) for both).

Remember you can have multiple tags for a given sha, so you could tag it
`v1.0.0-alpha` to start with and promote it to `v1.0.0`, if that is your
cup of tea.

Many thanks to the collective wisdom of Michiel Borkent
[@borkdude](https://twitter.com/borkdude), Alex Miller
[@puredanger](https://twitter.com/puredanger), [Sean
Corfield](https://github.com/seancorfield) and Erik Assum
[@slipset](https://twitter.com/slipset) on the clojurians slack.


*Original post below*

<hr>


When you want to consume a library using git dependencies, you go to the
project's GitHub page, lookup the SHA from the `README`, put it in your
`deps.edn`, and your done, right? -- But what happens when you want to
upgrade? rinse and repeat?  How do you even know that a new release is
available?

## On Git

A git repository is an append only log of changes to a project, and
Together with the repository url, the SHA forms a content based
addressing scheme to a particular state of the project.  This is a
natural identifier for that particular project state.

As consumers of a library, we aren't concerned with every single commit
made to the repository - we want to know the SHA that the project's
maintainers consider to be a release.  We might not want the main
branch HEAD commit, depending on the branching model used by the
project developers.

## A CI Pipeline

A good CI pipeline takes an immutable project artifact, and put's it
through increasingly vigorous testing.  It might start of as an alpha,
or a release candidate, and as confidence is increased through testing,
it can be promoted to a full release.

An artifact built from the contents of a single git SHA fits this model
nicely.  In an open source world, we can think of a SHA as an alpha
release, that gets tested by a small number of people, and then gets
published as a release - tools.build seems to follow this model for
example, with announcement of alphas on #tools.build, followed by
release announcements, for the same SHA, on #announce after a few people
have tried it.

## A new SHA, a new Version?

So which SHA do we want to put in our `deps.edn`?

In the maven world, versions are ordered, so when a new version is
published, it is a signal that can be used by tooling to determine if an
update is available.

In the git world, a SHA is not ordered, so how do we know when a new
version is available?  Should we check slack, or a blog, or the
project's home page? or could we as project authors provide data to
allow automation of this process?

## release.edn

To provide release information, we could put the version information
into the repository itself.

There are many release schemes, but we can model a project's releases as
falling into release streams.  Examples of this are "stable" or
"alpha" or "v4.x".  A release for each stream is then just a SHA
associated with the stream.

A natural way to present this would be as a map in an EDN file (or JSON,
or YAML, this doesn't need to be clojure specific).

``` clojure
{:stable {:git/tag "v1.0.0" :git/sha "abcdef"}
 :alpha {:git/tag "v1.1.0" :git/sha "abcdef"}
 :head {:git/tag "master" :git/sha :latest}}
```

A polylith or other monolith repository could have different streams for
the various artifacts it published.

If we decide on this format, we then need to make the file discoverable.
One way one be to take it from a git repository's default branch, which
seems like a good default.

The final piece of the puzzle would be for tooling like
[antq](https://github.com/liquidz/antq) and
[clojure-dependency-update-action](https://github.com/nnichols/clojure-dependency-update-action)
to use this information.


## Good idea?

I'm sure I can't be the first to have thought of this.

What do you think - is this useful? How could the idea be improved?
