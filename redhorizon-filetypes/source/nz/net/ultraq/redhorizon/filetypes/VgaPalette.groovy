/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes

import groovy.transform.InheritConstructors
import groovy.transform.Memoized

/**
 * An 18-bit VGA colour palette.  VGA used 6-bits per channel (for a total of
 * 18-bits of colour), so colour byte values fall into the 0-63 range.  To match
 * modern graphics, colour values are left-shifted by 2 so they can cover 0-255.
 * 
 * @author Emanuel Rabina
 */
@InheritConstructors
class VgaPalette extends Palette {

	@Memoized
	@Override
	byte[] getAt(int index) {

		return super.getAt(index).collect { it << 2 }
	}
}
