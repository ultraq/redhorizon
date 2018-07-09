/*
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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
 * Declares the methods other than the JNI entry-point method.
 * 
 * @author Emanuel Rabina
 */

// The various BigNumber types used throughout: 4, 64, and 130 int sized
typedef unsigned int BigNumber[64];
typedef unsigned int BigNumber4[4];
typedef unsigned int BigNumber130[130];

// Pre-calculation stuff
static void initPublicKey();
static void initTwoInts(BigNumber, int);

// Key calculations and methods
static void calculateBigNumber(BigNumber, BigNumber, BigNumber, int);
static void calculateKey(BigNumber, BigNumber, BigNumber, BigNumber, int);
static void dataLength(BigNumber, char *, int);
static unsigned int getMulShort(BigNumber);
static void moveDataToKey(BigNumber, char *, int, int);
static int  predataLength();
static void predataProcessing(const void *, int, unsigned char *);
static void clearTempVars(int);

// BigNumber calculations and methods
static void initBigNumber(BigNumber, unsigned int, int);
static int  bignumberBitLength(BigNumber, int);
static int  bignumberCompare(BigNumber, BigNumber, int);
static void bignumberDecrement(BigNumber, int);
static void bignumberIncrement(BigNumber, int);
static int  bignumberIntLength(BigNumber, int);
static void bignumberInverse(BigNumber, BigNumber, int);
static void bignumberMove(BigNumber, BigNumber, int);
static void bignumberMultiply(BigNumber, BigNumber, BigNumber, int);
static void bignumberMultiplyWord(BigNumber, BigNumber, unsigned int, int);
static void bignumberNegate(BigNumber, int);
static void bignumberNot(BigNumber, int);
static void bignumberShiftLeft(BigNumber, int, int);
static void bignumberShiftRight(BigNumber, int, int);
static int  bignumberSubtract(BigNumber, BigNumber, BigNumber, int, int);
