
Red Horizon - Codecs
====================

A collection of encoders/decoders for C&C file data.  The codecs here are used
mostly by the `redhorizon-filetypes` packages and serve as working examples for
JVM programmers.


Codecs
------

### IMAADPCM_16bit

An IMA-APPCM compression scheme for 16-bit audio.

### LCW

"Lempel–Castle–Welch" compression, Westwood's response to Unisys enforcing their
patent on "Lempel–Ziv–Welch" compression (though it doesn't resemble it),
formerly known as "Format80".

### PackData

A combination of Base64 and LCW, used in the \*pack data sections of C&C map
files.

### RLEZero

A form of run-length encoding that only compresses runs of 0s.

### RunLengthEncoding

A simple compression scheme in which repeated data is compressed to the data and
how often it should be repeated.

### WSADPCM_8bit

A custom 8-bit ADPCM compression scheme developed by Westwood.

### XORDelta

A custom compression scheme, similar to LCW but taking into account a set of
base data on which to XOR over to get the final output.  Formerly known as
"Format40".
