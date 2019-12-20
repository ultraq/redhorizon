
// ===================================
// Scanner's Java - Common C&C mission
// ===================================

package redhorizon.game.mission;

import redhorizon.filetypes.ini.IniFile;
import static redhorizon.game.mission.MissionCallbackItems.*;

/**
 * Abstraction of the C&C mission type to include components common to both RA
 * and TD.
 * 
 * @author Emanuel Rabina
 */
public abstract class MissionCNC extends Mission {

	// List of sections common to most missions
	static final String SECTION_BASIC        = "Basic";
	static final String SECTION_WAYPOINTS    = "Waypoints";
	static final String SECTION_CELLTRIGGERS = "CellTriggers";
	static final String SECTION_TEAMTYPES    = "TeamTypes";
	static final String SECTION_TRIGGERS     = "Triggers";
	static final String SECTION_BRIEFING     = "Briefing";

	// List of parameters common to most missions
	static final String PARAM_INTRO  = "Intro";
	static final String PARAM_BRIEF  = "Brief";
	static final String PARAM_WIN    = "Win";
	static final String PARAM_LOSE   = "Lose";
	static final String PARAM_ACTION = "Action";
	static final String PARAM_THEME  = "Theme";

	static final String VALUE_NOVIDEO = "<none>";
	static final String VALUE_NOTHEME = "no theme";		// Case in-sensitive

	/**
	 * Constructor, creates and readies a mission from the given mission file.
	 * 
	 * @param missionfile File containing data about the mission.
	 * @param callback	  Callback to notify of loading events.
	 */
	MissionCNC(IniFile missionfile, MissionCallback callback) {

		super(missionfile, callback);

		// Load parts common across all C&C mission types

		// Video loading
		String medianame = missionfile.getValue(SECTION_BASIC, PARAM_INTRO);
		introvidname = !medianame.equals(VALUE_NOVIDEO) ? medianame : null;
		callback.loaded(MISSION_VIDEO_INTRO, introvidname);

		medianame = missionfile.getValue(SECTION_BASIC, PARAM_BRIEF);
		briefingvidname = !medianame.equals(VALUE_NOVIDEO) ? medianame : null;
		callback.loaded(MISSION_VIDEO_BRIEFING, briefingvidname);

		medianame = missionfile.getValue(SECTION_BASIC, PARAM_ACTION);
		actionvidname = !medianame.equals(VALUE_NOVIDEO) ? medianame : null;
		callback.loaded(MISSION_VIDEO_ACTION, actionvidname);

		medianame = missionfile.getValue(SECTION_BASIC, PARAM_THEME);
		themename = !medianame.equalsIgnoreCase(VALUE_NOTHEME) ? medianame : null;
		callback.loaded(MISSION_THEME, themename);
	}
}
