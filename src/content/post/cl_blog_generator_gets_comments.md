---
title: "cl-blog-generator Gets Comments"
Pubdate: "2009-03-31"
tags: [ "cl-blog-generator", "lisp", "advogato"]
Description: "I have now added a comment system to cl-blog-generator.  My requirements were for a simple, low overhead, commenting system, preferable one that could possibly be fully automated."
aliases: ["/post/2009/.xhtml"]
---
<p>I have now added a comment system to <a href="http://github.com/hugoduncan/cl-blog-generator">cl-blog-generator</a>.  My requirements were for a simple, low overhead, commenting system, preferable one that could possibly be fully automated.</p>

<p>The comment system was inspired by <a href="http://www.steve.org.uk/Software/chronicle/">Chronicle</a>'s, with a slight modification in approach - the comments are never saved on the web server, and are just sent by email to a dedicated email address.  Spam filtering is delegated to the whatever spam filtering is implemented on the mail server, or in your email client.  The comment emails are then processed in CL using <a href="http://common-lisp.net/project/mel-base/">mel-base</a> and written to the local filesystem.  Moderation can optionally occur on the CL side, if that is preferable to using the email client.</p>

<p>There is still some work left to do - I would like to be able to switch off comments on individual posts, either on demand on after a default time period - but I thought I would let real world usage drive my development.</p>
