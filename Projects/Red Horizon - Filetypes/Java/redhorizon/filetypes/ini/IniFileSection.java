
package redhorizon.filetypes.ini;

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
