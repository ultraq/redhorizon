
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
