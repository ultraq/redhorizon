
package redhorizon.filetypes.mix;

import java.nio.ByteBuffer;

/**
 * Interface class for the native C++ functions used to obtain the 56-byte
 * Blowfish key from the 80-byte key source found in Red Alert MIX files.
 *  
 * @author Emanuel Rabina
 */
public class MixFileKey {

	static {
		System.loadLibrary("MixFileKey");
	}

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private MixFileKey() {
	}

	/**
	 * Calculates the 56-byte Blowfish key from the 80-byte key source found in
	 * Red Alert's MIX files.
	 * 
	 * @param source A buffer containing the 80-byte key source.
	 * @param dest   A buffer store for the 56-byte Blowfish key.
	 */
	public static void getBlowfishKey(ByteBuffer source, ByteBuffer dest) {

		source.rewind();
		byte[] d = new byte[56];
		getBlowfishKey(source.array(), d);
		dest.put(d).rewind();
	}

	/**
	 * This is the entry method for the public key -> private key function. A
	 * byte array of the 80-byte key source from the MIX file is given, and the
	 * 56-byte Blowfish key is calculated.
	 * 
	 * @param source A byte[] containing the 80-byte key source.
	 * @param dest   A byte[] store for the 56-byte Blowfish key.
	 */
	private native static void getBlowfishKey(byte[] source, byte[] dest);
}
