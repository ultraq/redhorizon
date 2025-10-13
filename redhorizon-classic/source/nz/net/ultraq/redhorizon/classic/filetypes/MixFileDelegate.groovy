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

package nz.net.ultraq.redhorizon.classic.filetypes

import groovy.transform.PackageScope

/**
 * Common class for MIX file delegates.
 *
 * @author Emanuel Rabina
 */
@PackageScope
abstract class MixFileDelegate {

	/**
	 * The number of bytes to adjust an entry offset value by, which is the number
	 * of bytes in the MIX file before the first entry.
	 */
	abstract int getBaseEntryOffset()

	/**
	 * The index of entries packaged within the MIX file.
	 */
	abstract MixEntry[] getEntries()
}
