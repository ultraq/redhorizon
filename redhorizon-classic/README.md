
Red Horizon - Classic
=====================

[![Maven Central](https://img.shields.io/maven-central/v/nz.net.ultraq.redhorizon/redhorizon-classic)](https://central.sonatype.com/artifact/nz.net.ultraq.redhorizon/redhorizon-classic)

A module that bridges the classic C&C file formats for use with the Red Horizon
engine.  All of the reverse engineering of the old codecs and files takes place
here, and serve as working examples for other programmers.


Installation
------------

Requires Java 21 on either macOS 12 Monterey and newer or Windows 10 64-bit and
newer.

### For Maven and Maven-compatible dependency managers

Add a dependency to your project with the following co-ordinates:

 - GroupId: `nz.net.ultraq.redhorizon`
 - ArtifactId: `redhorizon-classic`
 - Version: `0.41.4`


Codecs
------

The codecs (encoders/decoders) implemented in this module:

 - **[IMAADPCM_16bit](source/nz/net/ultraq/redhorizon/classic/codecs/IMAADPCM16bit.groovy)**:
   An IMA-ADPCM compression scheme for 16-bit audio.
 - **[LCW](source/nz/net/ultraq/redhorizon/classic/codecs/LCW.groovy)**:
   "Lempel–Castle–Welch" compression, Westwood's response to Unisys enforcing
   their patent on "Lempel–Ziv–Welch" compression (though it doesn't resemble
   it), formerly known as "Format80".
 - **[PackData](source/nz/net/ultraq/redhorizon/classic/codecs/PackData.groovy)**:
   A combination of Base64 and LCW, used in the \*pack data sections of C&C map
   files.
 - **[RLEZero](source/nz/net/ultraq/redhorizon/classic/codecs/RLEZero.groovy)**:
   A form of run-length encoding that only compresses runs of 0s.
 - **[RunLengthEncoding](source/nz/net/ultraq/redhorizon/classic/codecs/RunLengthEncoding.groovy)**:
   A simple compression scheme in which repeated data is compressed to the data
   and how often it should be repeated.
 - **[WSADPCM_8bit](source/nz/net/ultraq/redhorizon/classic/codecs/WSADPCM8bit.groovy)**:
   A custom 8-bit ADPCM compression scheme developed by Westwood.
 - **[XORDelta](source/nz/net/ultraq/redhorizon/classic/codecs/XORDelta.groovy)**:
   A custom compression scheme, similar to LCW but taking into account a set of
   base data on which to XOR over to get the final output.  Formerly known as
   "Format40".


Filetypes
---------

The older file formats implemented in this module and found in the [filetypes
package](source/nz/net/ultraq/redhorizon/classic/filetypes):

 - **AUD**:
   The sound format used by the classic C&C games.
 - **CPS**:
   A 320x200 image file format used lightly in Tiberium Dawn.
 - **MIX**:
   A package format, MIX files hold several other files in an uncompressed form.
 - **PAL**:
   A standard colour table format used by most programs of the era.
 - **SHP**:
   "Shape"(?) files, contain the images that comprise a unit or structure.
 - **TMP**:
   "Template" file for map tiles.
 - **VQA**:
   Full motion video file for briefing videos and cutscenes.
 - **WSA**:
   Full screen animations, contain no sound.


API
---

Browse the online groovydocs for the full API:
https://javadoc.io/doc/nz.net.ultraq.redhorizon/redhorizon-classic
