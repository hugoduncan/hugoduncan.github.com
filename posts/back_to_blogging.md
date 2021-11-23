> “My mind turned by anxiety, or other cause, from its scrutiny of blank
> paper, is like a lost child–wandering the house, sitting on the bottom
> step to cry.”  — Virginia Woolf

I was inspired to write some blog posts, which led me to realise my
current blog and blogging setup were completely broken.

Michiel Borkent ([@borkdude](https://twitter.com/borkdude)) recently
wrote about his
[migration from Octopress](https://blog.michielborkent.nl/migrating-octopress-to-babashka.html).
His requirements were very similar to mine, so I copied and modified.
Thank you Michiel!

His blog, [REPL adventures](https://blog.michielborkent.nl/), is well
worth the read.

## Blog Post Discussions

With a static web site, the perennial problem is how to enable
discussions.  Some people just punt pn this, and point to reddit, but
Michiel's solution is to use github discussions.  As a way of owning the
discussion, this has a lot of appeal.

I think it could be taken further.  It would be great if we could
automate the creation of a blog post topic when creating a blog post.
Unfortunately the [`gh`](https://cli.github.com/) command line client
doesn't support discussions yet, so that would require using Github's
GraphQL API - more work than I wanted to do for now.

One downside though, is that the discussions are not visible on the blog
pages, where discussion could easily engender more discussion. I wonder
if a Github Action could be triggered by conversation activity, and
automatically republish the post with the discussion to date at the end
of the post.


## Blogging Frameworks vs Tasks

There are many blog site generators (I used [Hugo](https://gohugo.io/),
of course), even if we limit ourselves to clojure:-
[bootleg](https://github.com/retrogradeorbit/bootleg),
[nota](https://github.com/rafaeldelboni/nota),
[cryogen](https://github.com/cryogen-project/cryogen), and
[static](https://github.com/nakkaya/static) to name a few.

These are usually feature rich.  The price for those features though, is
extra complexity.

Michiel's blog uses [babashka](https://book.babashka.org/) tasks to add
a post, render posts, etc. These are extremely quick to run and make
maintaining the blog simple.  it does just what he needs, and no more.

This reminds me of project automation, and the
[`tools.build`](https://clojure.org/guides/tools_build) approach of
using composable code tasks to build just what is needed.

Maybe there is an opportunity to take the same approach for building a
blog or static site. If we could pick from a selection of configurable
tasks, maybe we wouldn't need to write our own.

Speaking of which, I have tried writing my own before, in common-lisp:
[cl-blog-generator](http://github.com/hugoduncan/cl-blog-generator).

## And…

So I have a blogging setup.  Now I just need to write something.
