/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Adapted from code found in the documentation of the MIX format by author of
 * XCC Utilities, Olaf van der Spek, and commented with my understanding of
 * what is taking place.
 * 
 * No one is sure who wrote the original code, but judging from what it's
 * doing, it seems to be the function to convert the public key found in Red
 * Alert MIX files to the 56-byte private key required by the the Blowfish
 * encryption algorithm to decrypt the MIX headers.
 * 
 * @author (unknown)
 * @comments Emanuel Rabina
 */

#include <memory>
#include "MixFileKey.h"
#include "MixFileKeyJNI.h"

// Public key string and character map?
const static char* keystring = "AihRvNoIbTn85FZRYNZRcT+i6KpU+maCsEqr3Q5q+LDB5tH7Tz2qQ38V";
const static char char2num[] = {
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
	52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
	-1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
	15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
	-1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
	41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
};

// Public key
static struct {
	BigNumber key1;
	BigNumber key2;
	int length;
} PublicKey;

// Temp variables used in calculations
static BigNumber global1;
static int g1bitlength;
static int g1lengthx2;

static BigNumber4 g1hi;
static BigNumber4 g1hiinv;
static int g1hibitlength;
static unsigned int g1hiinvlo;
static unsigned int g1hiinvhi;

static BigNumber130 global2;

/**
 * This is the entry method for the public key -> private key function. A byte
 * array of the 80-byte key source from the MIX file is given, and the 56-byte
 * Blowfish key is calculated.
 * 
 * @param env    JNI environment variable.
 * @param cnckey Reference to the Java 'instance' of this static class.
 * @param source Byte array containing the 80-byte key source.
 * @param dest   Byte array store for the 56-byte Blowfish key.
 */
JNIEXPORT void JNICALL Java_redhorizon_filetypes_mix_MixFileKey_getBlowfishKey
	(JNIEnv *env, jclass cnckey, jbyteArray source, jbyteArray dest) {

	static bool publickeyinit = false;
	if (!publickeyinit) {
		initPublicKey();
		publickeyinit = true;
	}

	jbyte sourcebytes[80]; env->GetByteArrayRegion(source, 0, 80, sourcebytes);
	jbyte destbytes[56];   env->GetByteArrayRegion(dest, 0, 56, destbytes);

	unsigned char key[256];
	predataProcessing(sourcebytes, predataLength(), key);
	memcpy(destbytes, key, 56);

	env->SetByteArrayRegion(source, 0, 80, sourcebytes);
	env->SetByteArrayRegion(dest, 0, 56, destbytes);
}

/**
 * Initializes the variables in the PublicKey struct.  Namely, fills the
 * PublicKey.Key1 BigNumber with what appears to be a list of characters based
 * from the keystring variable, and the character map.
 */
static void initPublicKey() {

	char tempkey[256];
	initBigNumber(PublicKey.key2, 0x00010001, 64);

	for (int i1 = 0, i2 = 0; i1 < strlen(keystring); ) {

		int temp = 0;
		temp |= char2num[keystring[i1++]]; temp <<= 6;
		temp |= char2num[keystring[i1++]]; temp <<= 6;
		temp |= char2num[keystring[i1++]]; temp <<= 6;
		temp |= char2num[keystring[i1++]];

		tempkey[i2++] = (temp >> 16) & 0xff;
		tempkey[i2++] = (temp >> 8)  & 0xff;
		tempkey[i2++] =  temp        & 0xff;
	}

	dataLength(PublicKey.key1, tempkey, 64);
	PublicKey.length = bignumberBitLength(PublicKey.key1, 64) - 1;
}

/**
 * Not too sure on this method, but it looks as if it just initializes the
 * several temporary variables with some meaningful values.
 * 
 * @param bignum A BigNumber to source all the values from.
 * @param limit  Max array length variable.
 */
static void initTwoInts(BigNumber bignum, int limit) {

	bignumberMove(global1, bignum, limit);
	g1bitlength = bignumberBitLength(global1, limit);
	g1lengthx2 = (g1bitlength + 15) / 16;

	bignumberMove(g1hi, global1 + bignumberIntLength(global1, limit) - 2, 2);
	g1hibitlength = bignumberBitLength(g1hi, 2) - 32;

	bignumberShiftRight(g1hi, g1hibitlength, 2);
	bignumberInverse(g1hiinv, g1hi, 2);
	bignumberShiftRight(g1hiinv, 1, 2);

	g1hibitlength = (g1hibitlength + 15) % 16 + 1;
	bignumberIncrement(g1hiinv, 2);

	if (bignumberBitLength(g1hiinv, 2) > 32) {
		bignumberShiftRight(g1hiinv, 1, 2);
		g1hibitlength--;
	}
	g1hiinvlo = *(unsigned short *)g1hiinv;
	g1hiinvhi = *(((unsigned short *)g1hiinv) + 1);
}

/**
 * Calculates a BigNumber?  It seems to be a further step to the overall key
 * calculation, but I'm not sure what it does.
 * 
 * @param n1 -
 * @param n2 -
 * @param n3 -
 * @param limit Max array length variable.
 */
static void calculateBigNumber(BigNumber n1, BigNumber n2, BigNumber n3, int limit) {

	bignumberMultiply(global2, n2, n3, limit);
	global2[limit * 2] = 0;

	int g2lengthx2 = bignumberIntLength(global2, limit * 2 + 1) * 2;
	if (g2lengthx2 >= g1lengthx2) {
		bignumberIncrement(global2, limit * 2 + 1);
		bignumberNegate(global2, limit * 2 + 1);

		int lengthdiff = g2lengthx2 + 1 - g1lengthx2;

		unsigned short *esi = ((unsigned short *)global2) + (1 + g2lengthx2 - g1lengthx2);
		unsigned short *edi = ((unsigned short *)global2) + (g2lengthx2 + 1);

		for (; lengthdiff != 0; lengthdiff--) {
			esi--;
			edi--;
			unsigned short temp = getMulShort((unsigned int *)edi);

			if (temp > 0) {
				bignumberMultiplyWord((unsigned int *)esi, global1, temp, limit * 2);

				if ((*edi & 0x8000) == 0) {
					if (bignumberSubtract((unsigned int *)esi, (unsigned int *)esi, global1, 0, limit)) {
						(*edi)--;
					}
				}
			}
		}
		bignumberNegate(global2, limit);
		bignumberDecrement(global2, limit);
	}
	bignumberMove(n1, global2, limit);
}

/**
 * Performs calculations on the public key to generate the Blowfish key.  Most
 * of it seems to get off-loaded to the calculateBigNumber() method though.
 * 
 * @param n1 -
 * @param n2 -
 * @param n3 -
 * @param n4 -
 * @param limit Max array length variable.
 */
static void calculateKey(BigNumber n1, BigNumber n2, BigNumber n3, BigNumber n4, int limit) {

	BigNumber temp;

	initBigNumber(n1, 1, limit);

	int n4length = bignumberIntLength(n4, limit);
	initTwoInts(n4, n4length);

	int n3bitlength = bignumberBitLength(n3, n4length);
	int n3length = (n3bitlength + 31) / 32;

	unsigned int bitmask = (((unsigned int )1) << ((n3bitlength - 1) % 32)) >> 1;

	n3 += n3length - 1;
	n3bitlength--;

	bignumberMove(n1, n2, n4length);

	while (--n3bitlength != -1) {

		if (bitmask == 0) {
			bitmask = 0x80000000;
			n3--;
		}
		calculateBigNumber(temp, n1, n1, n4length);

		if (*n3 & bitmask) {
			calculateBigNumber(n1, temp, n2, n4length);
		}
		else {
			bignumberMove(n1, temp, n4length);
		}
		bitmask >>= 1;
	}
	initBigNumber(temp, 0, n4length);
	clearTempVars(limit);
}

/**
 * Attempts to discover the length of the key (in bytes) from the key string.
 * This is normally found in the 1st index position (data[1]) of the string.
 * But if the value at that position has it's sign bit set to 1, then discover
 * the length using a loop.
 * 
 * Once the length has been determined, a call moveDataToKey() is made to
 * begin moving bytes over.
 * 
 * @param key   The key which will take the bytes from the key string.
 * @param *data char* to the key string.
 * @param limit Max array length variable.
 */
static void dataLength(BigNumber key, char *data, int limit) {

	if (data[0] != 2) {
		return;
	}
	data++;

	int keylength;
	if (data[0] & 0x80) {
		keylength = 0;
		for (int i = 0; i < (data[0] & 0x7f); i++) {
			keylength = (keylength << 8) | data[i + 1];
		}
		data += (data[0] & 0x7f) + 1;
	}
	else {
		keylength = data[0];
		data++;
	}

	if (keylength <= limit * 4) {
		moveDataToKey(key, data, keylength, limit);
	}
}

/**
 * No damn idea as to what this does except to look confusing as hell.
 * 
 * @param bignum -
 * @return -
 */
static unsigned int getMulShort(BigNumber bignum) {

	unsigned short *wn = (unsigned short *)bignum;
	unsigned int i = (((((((((*(wn-1) ^ 0xffff) & 0xffff) * g1hiinvlo + 0x10000) >> 1)
			+ (((*(wn-2) ^ 0xffff) * g1hiinvhi + g1hiinvhi) >> 1) + 1)
			>> 16) + ((((*(wn-1) ^ 0xffff) & 0xffff) * g1hiinvhi) >> 1) +
			(((*wn ^ 0xffff) * g1hiinvlo) >> 1) + 1) >> 14) + g1hiinvhi
			* (*wn ^ 0xffff) * 2) >> g1hibitlength;

	if (i > 0xffff) {
		i = 0xffff;
	}
	return i & 0xffff;
}

/**
 * Transfers byte data from the key string to the key BigNumber.
 * 
 * @param key       The key which will take the bytes from the key string.
 * @param *data     char* to the key string.
 * @param keylength Byte length of the data.
 * @param limit     Max array length variable.
 */
static void moveDataToKey(BigNumber key, char *data, int keylength, int limit) {

	unsigned int sign;
	if (key[0] & 0x80) {
		sign = 0xff;
	}
	else {
		sign = 0;
	}

	int i;
	for (i = limit * 4; i > keylength; i--) {
		((unsigned char *)key)[i - 1] = sign;
	}
	for (; i > 0; i--) {
		((unsigned char *)key)[i - 1] = data[keylength - i];
	}
}

/**
 * The length of the byte values going into the predataProcessing() method?
 * Not too sure, although it does return some factor of 80 (40 on most
 * occassions, so maybe the number of times to perform key calculations in
 * the predataProcessing() method?)
 * 
 * @return The some value related to the length of the public key.
 */
static int predataLength() {

	int a = (PublicKey.length - 1) / 8;
	return (55 / a + 1) * (a + 1);
}

/**
 * Initializes some temporary BigNumber variables to be put through the various
 * private key functions/calculations, which then get copied to the destination
 * byte buffer for the Blowfish key.
 * 
 * @param source The 80-byte key source.
 * @param dest   Byte buffer for the 56-byte Blowfish key.
 */
static void predataProcessing(const void *voidsource, int predatalength, unsigned char *dest) {

	const unsigned char *source = reinterpret_cast<const unsigned char *>(voidsource);

	BigNumber n2;
	BigNumber n3;

	const int a = (PublicKey.length - 1) / 8;
	while (a + 1 <= predatalength) {

		initBigNumber(n2, 0, 64);
		memmove(n2, source, a + 1);
		calculateKey(n3, n2, PublicKey.key2, PublicKey.key1, 64);

		memmove(dest, n3, a);

		predatalength -= a + 1;
		source += a + 1;
		dest += a;
	}
}

/**
 * Sets all of the temporary variables back to 0.
 * 
 * @param limit Max array length variable.
 */
static void clearTempVars(int limit) {

	initBigNumber(global1, 0, limit);
	g1bitlength = 0;
	g1lengthx2 = 0;

	initBigNumber(g1hi, 0, 4);
	initBigNumber(g1hiinv, 0, 4);
	g1hibitlength = 0;
	g1hiinvlo = 0;
	g1hiinvhi = 0;

	initBigNumber(global2, 0, limit);
}

// **********************************
// BigNumber calculations and methods
// **********************************

/**
 * Initializes a BigNumber with all 0 values, then sets the given value as the
 * value of the first integer.
 */
static void initBigNumber(BigNumber bignum, unsigned int value, int length) {

	memset((void *)bignum, 0, length * 4);
	bignum[0] = value;
}

/**
 * Returns the number of bits being used to represent the value stored in the
 * given BigNumber.
 * 
 * @param bignum The BigNumber whose length is to be analyzed.
 * @param limit  Max array length variable.
 * @return The bit length of the BigNumber.
 */
static int bignumberBitLength(BigNumber bignum, int limit) {

	int intlength = bignumberIntLength(bignum, limit);
	if (intlength == 0) {
		return 0;
	}

	int bitlength = intlength * 32;
	unsigned int mask = 0x80000000;
	while ((mask & bignum[intlength - 1]) == 0) {
		mask >>= 1;
		bitlength--;
	}
	return bitlength;
}

/**
 * Comparison of 2 BigNumbers, returning -1, 0, or 1 if the first BigNumber is
 * less-than, equal-to, or greater-than the second BigNumber.
 * 
 * @param bignum1 Left-hand BigNumber for comparison.
 * @param bignum2 Right-hand BigNumber for comparison.
 * @param limit   Max array length variable.
 * @return -1, 0, 1 if the first BigNumber is less-than, equal-to, or
 *         greater-than the second BigNumber.
 */
static int bignumberCompare(BigNumber bignum1, BigNumber bignum2, int limit) {

	bignum1 += limit - 1;
	bignum2 += limit - 1;

	while (limit > 0) {
		if (*bignum1 < *bignum2) {
			return -1;
		}
		if (*bignum1 > *bignum2) {
			return 1;
		}
		bignum1--;
		bignum2--;
		limit--;
	}
	return 0;
}

/**
 * Decrements the value of the BigNumber by 1.
 * 
 * @param bignum The BigNumber to subtract 1 from.
 * @param limit  Max array length variable.
 */
static void bignumberDecrement(BigNumber bignum, int limit) {

	while ((--*bignum == 0xffffffff) && (--limit > 0)) {
		bignum++;
	}
}

/**
 * Increments the value of the BigNumber by 1.
 * 
 * @param bignum The BigNumber to add 1 to.
 * @param limit  Max array length variable.
 */
static void bignumberIncrement(BigNumber bignum, int limit) {

	while ((++*bignum == 0) && (--limit > 0)) {
		bignum++;
	}
}

/**
 * Returns the number of non-zero int values stored in the given BigNumber to
 * determine it's length.
 * 
 * @param bignum The BigNumber whose length is to be analyzed.
 * @param limit  Max array length variable.
 * @return The int length of the BigNumber.
 */
static int bignumberIntLength(BigNumber bignum, int limit) {

	int i = limit - 1;
	while ((i >= 0) && (bignum[i] == 0)) {
		i--;
	}
	return i + 1;
}

/**
 * Inverse function?
 * 
 * @param dest   The result of inverting the source BigNumber.
 * @param source The original BigNumber to be inverted.
 * @param limit  Max array length variable.
 */
static void bignumberInverse(BigNumber dest, BigNumber source, int limit) {

	BigNumber temp;
	int bytelength;
	int bitlength;
	unsigned int bit;

	initBigNumber(temp, 0, limit);
	initBigNumber(dest, 0, limit);

	bitlength = bignumberBitLength(source, limit);
	bit = ((unsigned int)1) << (bitlength % 32);
	dest += ((bitlength + 32) / 32) - 1;
	bytelength = ((bitlength - 1) / 32) * 4;
	temp[bytelength / 4] |= ((unsigned int)1) << ((bitlength - 1) & 0x1f);

	while (bitlength-- > 0) {
		bignumberShiftLeft(temp, 1, limit);
		if (bignumberCompare(temp, source, limit) != -1) {
			bignumberSubtract(temp, temp, source, 0, limit);
			*dest |= bit;
		}
		bit >>= 1;
		if (bit == 0) {
			dest--;
			bit = 0x80000000;
		}
	}
	initBigNumber(temp, 0, limit);
}

/**
 * Moves the values stored in one BigNumber to another.
 * 
 * @param dest   BigNumber to receive the source's values.
 * @param source BigNumber to source the values from.
 * @param limit  Max array length variable.
 */
static void bignumberMove(BigNumber dest, BigNumber source, int limit) {

	memmove(dest, source, limit * 4);
}

/**
 * Multiplies 2 BigNumbers together.
 * 
 * @param dest    BigNumber to hold the result of the multiplication.
 * @param source1 BigNumber to be multiplied by.
 * @param source2 BigNumber to multiply by.
 * @param limit   Max array length variable.
 */
static void bignumberMultiply(BigNumber dest, BigNumber source1, BigNumber source2, int limit) {

	initBigNumber(dest, 0, limit * 2);

	for (int i = 0; i < limit * 2; i++) {
		bignumberMultiplyWord(dest, source1, *(unsigned short *)source2, limit * 2);

		source2 = (unsigned int *)(((unsigned short *)source2) + 1);
		dest    = (unsigned int *)(((unsigned short *)dest) + 1);
	}
}

/**
 * Multiplies a BigNumber by an short-sized variable.  The BigNumber
 * multiplication method breaks-up its large task by doing several of these
 * smaller ones.
 * 
 * @param dest    BigNumber to hold the result of the multiplication.
 * @param source1 BigNumber to be multiplied by.
 * @param source2 Smaller short-sized value to multiply the BigNumber by.
 * @param limit   Max array length variable.
 */
static void bignumberMultiplyWord(BigNumber dest, BigNumber source1, unsigned int source2, int limit) {

	unsigned int temp = 0;

	for (int i = 0; i < limit; i++) {
		temp = source2 * (*(unsigned short *)source1) + *(unsigned short *)dest + temp;
		*(unsigned short *)dest = temp;

		dest = (unsigned int *)(((unsigned short *)dest) + 1);
		source1 = (unsigned int *)(((unsigned short *)source1) + 1);
		temp >>= 16;
	}
	*(unsigned short *)dest += temp;
}

/**
 * The negation of a BigNumber (n -> -n).
 * 
 * @param bignum The BigNumber to negate.
 * @param limit  Max array length variable.
 */
static void bignumberNegate(BigNumber bignum, int limit) {

	bignumberNot(bignum, limit);
	bignumberIncrement(bignum, limit);
}

/**
 * 'Nots' a BigNumber.  Basically turns the given BigNumber into it's binary
 * complement.
 * 
 * @param bignum The BigNumber to 'not'.
 * @param limit  Max array length variable.
 */
static void bignumberNot(BigNumber bignum, int limit) {

	for (int i = 0; i < limit; i++) {
		*(bignum++) = ~*bignum;
	}
}

/**
 * Shifts the bits stored in the given BigNumber left by the specified amount.
 * 
 * Note: I belive this implementation of left shifting has a bug in it as the
 *       right-most bits in the 0th int value are not properly moved when the
 *       shift amount is a factor of 32.  The bugged lines have been commented
 *       out and replaced, but kept for reference.
 * 
 * @param bignum The BigNumber whose bits are to be shifted left.
 * @param shift  Number of bits to shift left by.
 * @param limit  Max array length variable.
 */
static void bignumberShiftLeft(BigNumber bignum, int shift, int limit) {

	int i1;
	int i2 = shift / 32;

	if (i2 > 0) {
//		for (i1 = limit - 1; i1 > i2; i1--) {
		for (i1 = limit - 1; i1 >= i2; i1--) {
			bignum[i1] = bignum[i1 - i2];
		}
//		for (; i1 > 0; i1--) {
		for (; i1 >= 0; i1--) {
			bignum[i1] = 0;
		}
		shift %= 32;
	}

	if (shift == 0) {
		return;
	}

	for (i1 = limit - 1; i1 > 0; i1--) {
		bignum[i1] = (bignum[i1] << shift) | (bignum[i1 - 1] >> (32 - shift));
	}
	bignum[0] <<= shift;
}

/**
 * Shifts the bits stored in the given BigNumber right by the specified amount.
 * 
 * @param bignum The BigNumber whose bits are to be shifted right.
 * @param shift  Number of bits to shift right by.
 * @param limit  Max array length variable.
 */
static void bignumberShiftRight(BigNumber bignum, int shift, int limit) {

	int i1;
	int i2 = shift / 32;

	if (i2 > 0) {
		for (i1 = 0; i1 < limit - i2; i1++) {
			bignum[i1] = bignum[i1 + i2];
		}
		for (; i1 < limit; i1++) {
			bignum[i1] = 0;
		}
		shift %= 32;
	}

	if (shift == 0) {
		return;
	}

	for (i1 = 0; i1 < limit - 1; i1++) {
		bignum[i1] = (bignum[i1] >> shift) | (bignum[i1 + 1] << (32 - shift));
	}
	bignum[i1] = bignum[i1] >> shift;
}

/**
 * Subtraction of 1 BigNumber from another.
 * 
 * @param dest    BigNumber which will hold the result of the subtraction.
 * @param source1 BigNumber which will be subtracted from.
 * @param source2 BigNumber to subtract the first one by.
 * @param carry   Carry-over value, 0 for positive, 1 for negative.
 * @param limit   Max array length variable.
 */
static int bignumberSubtract(BigNumber dest, BigNumber source1, BigNumber source2, int carry, int limit) {

	unsigned int i1;
	unsigned int i2;
	limit += limit;

	while (--limit != -1) {
		i1 = *(unsigned short *)source1;
		i2 = *(unsigned short *)source2;
		*(unsigned short *)dest = i1 - i2 - carry;

		source1 = (unsigned int *)(((unsigned short *)source1) + 1);
		source2 = (unsigned int *)(((unsigned short *)source2) + 1);
		dest    = (unsigned int *)(((unsigned short *)dest) + 1);

		if ((i1 - i2 - carry) & 0x00010000) {
			carry = 1;
		}
		else {
			carry = 0;
		}
	}
	return carry;
}
