# trak

A toy project to teach myself/explore Clojurescript

## Overview

An idea:

- All "business logic" happens via `core.async` pub/sub
- Views only react to database changes 

This may be too convoluted and I might rethink the approach, but why not :)

### What database?

[Datascript](https://github.com/tonsky/datascript). Read the author's
own blog posts on it to see what is going on (all of them backed up by source 
code): 

- [Chatting cats use DataScript for fun](http://tonsky.me/blog/datascript-chat/)
- [Another powered-by-DataScript example](http://tonsky.me/blog/acha-acha/)

Using Datascript's `d/listen!` we re-render the app when database is updated

### What pub/sub?

[core.async Pub Sub](https://github.com/clojure/core.async/wiki/Pub-Sub). 
A `core.async` channel is acting as a message queue. You can publish messages
to a topic in this queue. You can subscribe to the queue and listen to messages
in a topic. Yes, this works even in the browser.

Got this idea from [Chatting cats use DataScript for fun](http://tonsky.me/blog/datascript-chat/)

This way you can implement the entire [Redux](http://redux.js.org) architecture
in ten lines of code.

## What/how?


## Setup

Copy `profiles.clj.sample` to `profiles.clj` and edit `:env` values to your 
liking.

**Note: This is a client-side only app. So any secrets you define there
 will be injected into the page and visible to anyone with access to
 the page's Javascript.**

To get an interactive development environment run:

    lein figwheel

or

    rlwrap lein figwheel

for a better REPL

Open your browser at [localhost:3449](http://localhost:3449/).

## License

MIT, see [LICENSE](./LICENSE)
