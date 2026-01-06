
Red Horizon
===========

[![Build Status](https://github.com/ultraq/redhorizon/actions/workflows/build.yml/badge.svg)](https://github.com/ultraq/redhorizon/actions)
[![GitHub Release](https://img.shields.io/github/release/ultraq/redhorizon.svg?maxAge=3600)](https://github.com/ultraq/redhorizon/releases/latest)

A 2D game engine built to recreate many aspects of the Command & Conquer games
of the 1990s, or at least have a lot of fun playing with the C&C assets while I
learn about how one goes about building a game engine 😄

This project has gone through a few incarnations with changes in goal and scope
since its inception way back in 2007.  There's a [project history](#project-history)
below for those interested, but as of writing the current goal is building a
game engine as a way to learn about other areas of programming not related to my
job (I'm a web developer by day) and learning about new and different technical
aspects in the process.


Installation
------------

This project consists of several modules in an attempt to prove out various
parts of the overall engine.  These standalone modules are:

 - [`redhorizon-audio`](redhorizon-audio) - play sounds and music
 - [`redhorizon-classic`](redhorizon-classic) - bridge C&C files and assets for everything here
 - [`redhorizon-engine`](redhorizon-engine) - make everything here interactive
 - [`redhorizon-graphics`](redhorizon-graphics) - draw meshes and sprites
 - [`redhorizon-input`](redhorizon-input) - read and respond to player input
 - [`redhorizon-scenegraph`](redhorizon-scenegraph) - organize objects in a game world

Instructions for use in projects can be found in each of those subproject
READMEs.

### Other releases

The very old file conversion tools from the first era of this project can be
found in the [Pre-GitHub releases tag](https://github.com/ultraq/redhorizon/releases/tag/pre-github-releases)

There are several other tags/releases but were really just for my own benefit
until the 0.40.0 tag where I start to put things up on Maven central for use in
other projects to aid with my learning of game engine development.

After that, [the `redhorizon-classic` project](redhorizon-classic) is likely the
most interesting as it has code and working examples of reading the classic C&C
codecs and file formats.


Project history
---------------

This project started way back in 2007 as an attempt by me, a naive and fresh-out-of-uni
programmer, to recreate the original 2D Command & Conquer (C&C) games which I
was very familiar with having spent much of my teenage years modding and
creating campaigns for them.

In the first couple of years I managed to display the map of the first Allied
mission from C&C Red Alert (RA) using fixed-function OpenGL on Windows, from
which came a set of file conversion tools to decode various file formats from
those games.  Not being able to do much beyond a static display though, I got
discouraged and this project lay dormant for a very long time.

![splash-screen](https://images.ultraq.net.nz/redhorizon/screenshots/GUI_SplashScreen.png)
![drawing-map](https://images.ultraq.net.nz/redhorizon/screenshots/InGame_Structures.png)
![view-dune2-files](https://images.ultraq.net.nz/redhorizon/screenshots/Utils_ReadDune2SHP.png)

Things started back up in late 2019 where I got the itch to update the old
Windows batch files that were used for converting files, to cross-platform CLI
tools (I was now running a Mac, which couldn't run the old stuff).  In the
process I developed some basic 'media player' tools which made me learn that
the way graphics were rendered in the early 2000s are not at all the they are
rendered now!  So 2020 was spent re-learning modern OpenGL with shaders and
rewriting the graphics around that.

[![Red Horizon - Play WSA file demo](https://i.ytimg.com/vi_webp/mp7A6EMWupY/maxresdefault.webp)](https://www.youtube.com/watch?v=mp7A6EMWupY)

[![Red Horizon - Play VQA file demo](https://i.ytimg.com/vi_webp/3jpLoEJ22xc/maxresdefault.webp)](https://www.youtube.com/watch?v=3jpLoEJ22xc)

Even with new technology I was still stuck with static objects and didn't have
much in the way of making things move or have complicated responses to player
input without creating absolute messes of code.  So from 2023 onwards my focus
shifted to learning about the engine development side of game development as a
way to learn about the systems and architectures used to make games interactive.

This is where we are now, and the 0.40.0 and newer tags are a marker of this
shift in focus where I've been using tutorials made for other game engines as a
way to prove that what I'm doing can actually work.  The projects are simple,
but built so far using Red Horizon as their basis are:

[redhorizon-libgdx-simplegame](https://github.com/ultraq/redhorizon-libgdx-simplegame)
[![redhorizon-libgdx-simplegame](https://raw.githubusercontent.com/ultraq/redhorizon-libgdx-simplegame/refs/heads/main/screenshot.png)](https://github.com/ultraq/redhorizon-libgdx-simplegame)

[redhorizon-unity-asteroids](https://github.com/ultraq/redhorizon-unity-asteroids)
[![redhorizon-unity-asteroids](https://raw.githubusercontent.com/ultraq/redhorizon-unity-asteroids/refs/heads/main/screenshot-main.png)](https://github.com/ultraq/redhorizon-unity-asteroids)

And there is also [redhorizon-shooter](https://github.com/ultraq/redhorizon-shooter)
where I'm really just having a play with C&C's assets to do whatever thing I can
think of in a twin-stick shooter environment.
