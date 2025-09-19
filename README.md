
Red Horizon
===========

[![Build Status](https://github.com/ultraq/redhorizon/actions/workflows/build.yml/badge.svg)](https://github.com/ultraq/redhorizon/actions)
[![GitHub Release](https://img.shields.io/github/release/ultraq/redhorizon.svg?maxAge=3600)](https://github.com/ultraq/redhorizon/releases/latest)

This repository is an archive of work I started several years ago to attempt to
recreate the original 2D Command & Conquer (C&C) games. The most useful outcome
of this project has been a set of file conversion tools and working code
examples of how to decode/encode various file formats from those games.

While I don't think I have the time any more to fulfil the original goal of
running the original Red Alert campaign missions with this (there's that [C&C
Remaster project](https://www.ea.com/en-gb/games/command-and-conquer/command-and-conquer-remastered)
now!), I update this project every so often when I feel the urge to work on
smaller things like graphics rendering, playing sounds, creating CLI tools, and
general software architecture for games as opposed to my day-to-day which is web
development.


Installation
------------

Currently, this project consists of several modules in an attempt to prove out
various parts of the overall engine. The first standalone modules are:

 - [`redhorizon-audio`](redhorizon-audio)
 - [`redhorizon-graphics`](redhorizon-graphics)
 - [`redhorizon-input`](redhorizon-input)

Instructions for use in projects can be found in each of those subproject
READMEs.

### Other releases

To use the CLI tools (file conversion, playing back audio/video, viewing
maps/units), check [the `redhorizon-cli` project](redhorizon-cli) for
installation instructions.

To work with classic C&C codecs and file formats in your own projects, check
[the `redhorizon-classic` project](redhorizon-classic) for installation
instructions.
