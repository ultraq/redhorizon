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

package nz.net.ultraq.redhorizon.filetypes.io

import groovy.transform.TupleConstructor

/**
 * Take an object and write it out to a file stream.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
abstract class FileWriter<T> {

	final OutputStream outputStream

	/**
	 * Write the given object to the current output stream.
	 * 
	 * @param source
	 * @param options
	 */
	abstract void write(T source, Map options)
}
