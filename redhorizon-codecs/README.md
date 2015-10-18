
Red Horizon - Codecs
====================

A collection of encoders/decoders for old C&C file data.  The codecs here are
used mostly by the `redhorizon-filetypes` packages and serve as working examples
for JVM programmers.


Installation
------------

Either build the project from the source code here on GitHub, or add it to your
project using the following Maven coordinates:

 - GroupId: `nz.net.ultraq.redhorizon`
 - ArtifactId: `redhorizon-codecs`
 - Version: `1.0.0-SNAPSHOT`


Codecs
------

### CodecUtility

Aggregates all of the codecs into this single class where static `encode`/`decode`
methods delegate to their respective codecs.

### Base64

Yet another Base64 codec.  Java 8 has since exposed their internal Base64
encoder/decoder classes, so this codec will likely be replaced by their one.

### Format2

A custom compression scheme that is basically run-length encoding on 0s.

### Format40

A custom compression scheme.

### Format80

A custom compression scheme.

### IMAADPCM_16bit

An IMA-APPCM compression scheme for 16-bit audio.

### PackData

A combination of Base64 and Format80, used in the \*pack data sections of C&C
map files.

### RunLengthEncoding

A simple compression scheme in which repeated data is compressed to the data and
how often it should be repeated.

### WSADPCM_8bit

A custom 8-bit ADPCM compression scheme developed by Westwood.
