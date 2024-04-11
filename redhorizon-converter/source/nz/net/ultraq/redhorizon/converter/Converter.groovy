/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.converter

import groovy.transform.TupleConstructor

/**
 * Converts one file type to another.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
abstract class Converter<I, O> {

	final I inputFile

	/**
	 * Perform conversion of the input file data to the output stream.
	 */
	abstract void convert(OutputStream outputStream, O options = null)
}
