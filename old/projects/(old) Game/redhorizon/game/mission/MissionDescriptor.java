
// ====================================
// Scanner's Java - Mission description
// ====================================

package redhorizon.game.mission;

import java.io.Serializable;

/**
 * Data structure for a mission, title, and implementing mission file.
 * 
 * @author Emanuel Rabina
 */
public class MissionDescriptor implements Serializable {

	private final String file;
	private final String name;
	private final String briefing;

	/**
	 * Constructor, assigns the mission's implementation file value, and the
	 * mission's name.
	 * 
	 * @param file Name of the file which contains this mission.
	 * @param name Name of the mission.
	 */
	public MissionDescriptor(String file, String name) {

		this(file, name, null);
	}

	/**
	 * Constructor, assigns the mission's implementation file value, the
	 * mission's name, and a briefing.
	 * 
	 * @param file	   Name of the file which contains this mission.
	 * @param name	   Name of the mission.
	 * @param briefing Locale-specific text for the mission briefing.  If this
	 * 				   is not supplied, then the briefing text will be taken
	 * 				   from the file.
	 */
	public MissionDescriptor(String file, String name, String briefing) {

		this.file     = file;
		this.name     = name;
		this.briefing = briefing;
	}

	/**
	 * Returns the mission briefing for this mission.
	 * 
	 * @return The mission briefing, or <code>null</code> if none has been
	 * 		   specified.
	 */
	public String getBriefing() {

		return briefing;
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
