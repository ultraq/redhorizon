
Changelog
=========

### 0.33.0
 - Merged all of the various CLI tools into a single one.  Now each tool is a
   subcommand of a main `redhorizon` one.
 - Implemented basic batch rendering and object culling to speed up rendering

### 0.32.0
 - Replaced the OpenGL fixed-function pipeline with a modern shader-based
   renderer.  This opens up Red Horizon to do paletted textures on the GPU.  A
   minimum of OpenGL 4.1 is now needed.
 - Added ImGui to include debug info, which was normally emitted in log files,
   right in the view.

### 0.31.2
 - Upgrade the reflections library which came with a change that removes the
   large Google Guava dependency - we're now 2MB lighter
 - Use the current desktop refresh rate in full screen mode instead of always
   replacing it with the highest the monitor can do
 - Added a resource manager which can search for items inside a directory and
   all its subdirectories, loosening one restriction of the map viewer only
   working with my current setup.  However, a new restriction is that it doesn't
   yet work on `.mix` files inside `.mix` files, so it's still somewhat specific
   to my workspace 😅
 - Sped up map rendering and reduced memory usage by reusing map tiles already
   encountered

### 0.31.1
 - Lean on Java's built-in blowfish cipher to replace the BlowfishJ library I'd
   been using.
 - Renamed the unit viewer to a general purpose object viewer, and gave it the
   ability to view Red Alert mission maps.  Currently only works with the
   directory structure I have for local dev and doesn't work for interior maps.

### 0.31.0
 - Converted the C/C++ code for reading encrypted MIX files to Groovy - no more
   native platform compilers needed for supporting other platforms
 - Rebuilt the file conversion utility (the [`convert` CLI tool](README.md#convert)),
   which currently supports the following file conversions:
    - PCX -> CPS
    - paletted PNG -> C&C SHP
 - Added more options to the media player, including less blurry filtering, a
   full screen mode, play/pause, and more
 - Created a unit/structure viewer which supports a handful of units that I have
   added configuration files for (see: the [`view` CLI tool](README.md#view))

### 0.30.3
 - Improve performance of VQA file playback
 - Allow playback of some VQA files in Red Alert that contain unknown data
 - Support for viewing SHP files (puts all the frames up on the screen at once)
   with either the RA/TD "temperate" palette
 - When viewing images/animations/videos, don't let the window get bigger than
   the smallest screen in a multi-monitor setup ([#31](https://github.com/ultraq/redhorizon/issues/31))

### 0.30.2
 - Support for playing VQA files through the media player
 - Performance improvements when streaming files, reducing memory usage by
   deleting old sound buffers / animation frames
 - Media player window is no longer fixed to 640x480, now growing to some value
   that is almost as large as the primary display, while maintaining the desired
   aspect ratio

### 0.30.1
 - Support for playing WSA files through the media player
 - Can now view images and animations on Windows
 - Performance improvements to not peg the CPU during graphics rendering

### 0.30.0

First release on GitHub! 🎉  What this release represents is an attempt to move
this project forward since I started it back in 2007 by targeting some smaller
goals and steps to bring it back up to a working state. The 0.30.x releases will
focus on being able to view the old C&C/RA content such as images, animations,
videos, sounds, music, etc, through some very basic CLI tools.

This release has the ability to read standard and encrypted (RA) mix files to
extract their contents to disk (via the mix CLI tool), and play AUD files and
view PCX files (via the play CLI tool).
