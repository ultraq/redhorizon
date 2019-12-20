
// ==================================
// Scanner's Java - Red Alert mission
// ==================================

package redhorizon.game.mission;

import redhorizon.filetypes.ini.IniFile;
import redhorizon.game.Rules;

/**
 * Red Alert -specific implementation of a {@link Mission}.
 * 
 * @author Emanuel Rabina
 */
public class MissionRA extends MissionCNC {

	/**
	 * Constructor, loads a mission from the given mission file.
	 * 
	 * @param missionfile File containing the mission.
	 * @param callback	  Callback to notify of significant loading events.
	 */
	@SuppressWarnings("unchecked")
	MissionRA(IniFile missionfile, MissionCallback callback) {

		super(missionfile, callback);

		// Override defaults with mission-specific rules
		Rules.currentRules().overrideDefaults(missionfile);
	}
}
