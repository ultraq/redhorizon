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

package nz.net.ultraq.redhorizon.classic.filetypes.shp

import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream

import groovy.transform.PackageScope

/**
 * Representation of an offset record found in SHP files.  It turns out that not
 * all SHP files are just straight images in their own right, but some form of
 * 'difference' data (think video difference encoding) which has to be matched
 * to a certain key image to obtain the full frame.  The link to which frame has
 * to be matched with which, is found in an offset.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
class ShpImageInfo {

	final int offset
	final byte offsetFormat
	final int refOff
	final byte refOffFormat

	/**
	 * Constructor, creates an offset record from data in the input stream.
	 * 
	 * @param input
	 */
	ShpImageInfo(NativeDataInputStream input) {

		def off1 = input.readInt()
		offsetFormat = (byte)(off1 >>> 24)
		offset = off1 & 0x00ffffff

		def off2 = input.readInt()
		refOffFormat = (byte)(off2 >>> 24)
		refOff = off2 & 0x00ffffff
	}
}
