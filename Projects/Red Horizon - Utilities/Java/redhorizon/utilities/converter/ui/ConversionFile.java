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

package redhorizon.utilities.converter.ui;

/**
 * Available conversion source file types.
 * 
 * @author Emanuel Rabina
 */
public enum ConversionFile {

	PCX         ("PCX"),
	PNG         ("PNG"),
	PNG_MULTIPLE("PNG (Multiple)"),
	SHP         ("SHP"),
	SHP_D2      ("SHP (Dune 2)"),
	WSA         ("WSA"),
	WSA_D2      ("WSA (Dune 2)");

	public final String name;
	public final boolean multipleinputs;

	/**
	 * Constructor, sets the source name.
	 * 
	 * @param name
	 */
	private ConversionFile(String name) {

		this.name           = name;
		this.multipleinputs = name.contains("Multiple");
	}
}
