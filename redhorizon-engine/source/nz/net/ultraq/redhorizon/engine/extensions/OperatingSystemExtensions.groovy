/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.extensions

/**
 * Extensions to the `System` class to add convenience methods for querying the
 * current platform.
 * 
 * @author Emanuel Rabina
 */
class OperatingSystemExtensions {

	/**
	 * Return whether the current processor has a 64-bit ARM architecture.
	 * 
	 * @param self
	 * @return
	 */
	static boolean isArm64(System self) {

		return System.getProperty('os.arch').contains('aarch64')
	}

	/**
	 * Return whether the current OS is macOS.
	 * 
	 * @param self
	 * @return
	 */
	static boolean isMacOs(System self) {

		return System.getProperty('os.name').toLowerCase().startsWith('mac')
	}

	/**
	 * Return whether the current OS is Windows.
	 * 
	 * @param self
	 * @return
	 */
	static boolean isWindows(System self) {

		return System.getProperty('os.name').startsWith('Windows')
	}
}
