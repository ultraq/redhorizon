/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.utilities.converter;

/**
 * Exception thrown by the converter classes to indicate that the conversion
 * couldn't be performed because it isn't supported.
 * 
 * @author Emanuel Rabina
 */
public class UnsupportedConversionException extends RuntimeException {

	/**
	 * Constructor, set a message and the cause of the conversion not being
	 * supported.
	 * 
	 * @param message
	 * @param cause
	 */
	public UnsupportedConversionException(String message, Throwable cause) {

		super(message, cause);
	}
}
