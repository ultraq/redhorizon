/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.extensions

/**
 * Extensions to the {@link BigInteger} class.
 * 
 * @author Emanuel Rabina
 */
class BigIntegerExtensions {

	/**
	 * Calculate the number of bytes needed to be able to represent this {@code
	 * BigInteger}, excluding space for a sign bit.
	 * 
	 * @param self
	 * @return
	 */
	static int byteLength(BigInteger self) {

		return (self.bitLength() + 7) >>> 3
	}

	/**
	 * Calculate the number of int-sized values needed to be able to represent
	 * this {@code BigInteger}, excluding space for a sign bit.  (The name differs
	 * slightly as there is already a private method in {@code BigInteger} with
	 * the intended name!)
	 * 
	 * @param self
	 * @return
	 */
	static int intLengthNoSign(BigInteger self) {

		return (self.bitLength() + 31) >>> 5
	}

	/**
	 * An alias for {@link BigInteger#shiftLeft} that allows the use of the
	 * {@code <<} symbol}.
	 * 
	 * @param self
	 * @param val
	 * @return
	 */
	static BigInteger leftShift(BigInteger self, int n) {

		return self.shiftLeft(n)
	}

	/**
	 * An alias for {@link BigInteger#shiftRight} that allows the use of the
	 * {@code >>} symbol.
	 * 
	 * @param self
	 * @param val
	 * @return
	 */
	static BigInteger rightShift(BigInteger self, int n) {

		return self.shiftRight(n)
	}

	/**
	 * Calculate the number of short-sized values needed to be able to represent
	 * this {@code BigInteger}, excluding space for a sign bit.
	 * 
	 * @param self
	 * @return
	 */
	static int shortLength(BigInteger self) {

		return (self.bitLength() + 15) >>> 4
	}
}
