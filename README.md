
Red Horizon
===========

[![Build Status](https://travis-ci.org/ultraq/redhorizon.svg)](https://travis-ci.org/ultraq/redhorizon)
[![License](https://img.shields.io/github/license/ultraq/redhorizon.svg?maxAge=2592000)](https://github.com/ultraq/redhorizon/blob/master/LICENSE.txt)

This repository is an archive of work I did several years ago to attempt to
recreate the orignal 2D Command & Conquer (C&C) games.  The most useful outcome
of this project has been a set of file conversion tools and working code
examples of how to decode/encode various file formats from those games.

While I don't think I have the time any more to fulfil the original goal of
running the original Red Alert campaign missions with this (there's that [C&C
Remaster project](https://www.ea.com/en-gb/games/command-and-conquer/command-and-conquer-remastered)
now!), I update this project every so often when I feel the urge to work on
smaller things like graphics rendering, playing sounds, creating good CLI tools,
and general software architecture for games as opposed to my day-to-day which is
web development.

These smaller goals can be observed as [milestones](https://github.com/ultraq/redhorizon/milestones)
on GitHub, so you can follow along there.


Installation
------------

Requires Java 11+ on macOS 10.12+ (Sierra+) or Windows 10 64-bit.

Download the distribution bundle from the [project releases](https://github.com/ultraq/redhorizon/releases)
page, then unzip it to somewhere on your computer.


Usage
-----

Open up a console to where you extracted the bundle.  From there, you can run
the Mix file reader by running `bin/mix` or the Media Player by running
`bin/play`.  Either by supplying a `--help`/`-h` option or by entering no
parameters, the help text should be printed out showing you how to use these
commands.

[![Red Horizon - Play WSA file demo](screenshot-of-wsa-demo.png)](https://www.youtube.com/watch?v=mp7A6EMWupY)
