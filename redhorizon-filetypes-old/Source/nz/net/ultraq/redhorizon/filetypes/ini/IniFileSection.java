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

package nz.net.ultraq.redhorizon.filetypes.ini;

import java.util.HashMap;

/**
 * A section of key/value properties in an INI file.
 * 
 * @author Emanuel Rabina
 */
public class IniFileSection extends HashMap<String,String> {

	private final String name;

	/**
	 * Constructor, set the name of this section.
	 * 
	 * @param name
	 */
	IniFileSection(String name) {

		this.name = name;
	}

	/**
	 * A section name is equal to another if they share the same name.
	 * 
	 * @param other
	 * @return <tt>true</tt> if the other object is a section with the same
	 * 		   name.
	 */
	@Override
	public boolean equals(Object other) {

		return other instanceof IniFileSection && name.equals(((IniFileSection)other).name);
	}

	/**
	 * Return this section's name.
	 * 
	 * @return Section name.
	 */
	public String getName() {

		return name;
	}
}
