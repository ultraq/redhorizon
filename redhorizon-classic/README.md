
Red Horizon - Classic
=====================

A module that bridges the classic C&C file formats for use with the Red Horizon
engine.  All of the reverse engineering of the old codecs and files takes place
here, and serve as working examples for other programmers.


Codecs
------

The codecs (encoders/decoders) implemented in this module:

 - **[IMAADPCM_16bit](source/nz/net/ultraq/redhorizon/classic/codecs/IMAADPCM16bit.groovy)**:
   An IMA-APPCM compression scheme for 16-bit audio.
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

The older file formats implemented in this module:

 - **[AUD](source/nz/net/ultraq/redhorizon/classic/filetypes/aud)**:
   The sound format used by the classic C&C games.
 - **[CPS](source/nz/net/ultraq/redhorizon/classic/filetypes/cps)**:
   A 320x200 image file format used lightly in Tiberium Dawn.
 - **[MIX](source/nz/net/ultraq/redhorizon/classic/filetypes/mix)**:
   A package format, MIX files hold several other files in an uncompressed form.
 - **[PAL](source/nz/net/ultraq/redhorizon/classic/filetypes/pal)**:
   A standard colour table format used by most programs of the era.
 - **[SHP](source/nz/net/ultraq/redhorizon/classic/filetypes/shp)**:
   "Shape"(?) files, contain the images that comprise a unit or structure.
 - **[TMP](source/nz/net/ultraq/redhorizon/classic/filetypes/tmp)**:
   "Template" file for map tiles.
 - **[VQA](source/nz/net/ultraq/redhorizon/classic/filetypes/vqa)**:
   Full motion video file for briefing videos and cutscenes.
 - **[WSA](source/nz/net/ultraq/redhorizon/classic/filetypes/wsa)**:
   Full screen animations, contain no sound.
