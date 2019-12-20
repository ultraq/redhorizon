
// ================================
// Scanner's Java - Map description
// ================================

package redhorizon.game.map;

import java.io.Serializable;

/**
 * Data structure for a map, name, and implementing map file.
 * 
 * @author Emanuel Rabina
 */
public class MapDescriptor implements Serializable {

	protected final String file;
	protected final String name;

	/**
	 * Constructor, assigns the mission's implementation file value, and the
	 * mission's name.
	 * 
	 * @param file Name of the file which contains this mission.
	 * @param name Name of the mission.
	 */
	public MapDescriptor(String file, String name) {

		this.file = file;
		this.name = name;
	}

	/**
	 * Returns the name of the mission's implementing file.
	 * 
	 * @return Mission file.
	 */
	public String getFile() {

		return file;
	}

	/**
	 * Returns the name of the mission.
	 * 
	 * @return Mission name.
	 */
	public String getName() {

		return name;
	}
}
