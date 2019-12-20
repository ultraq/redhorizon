
// =============================
// Scanner's Java - Mission file
// =============================

package redhorizon.game.mission;

import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.MissionFile;
import redhorizon.filetypes.ini.IniFile;
import redhorizon.game.GameFlowListener;
import redhorizon.media.SoundTrack;
import redhorizon.misc.CNCGameTypes;

/**
 * This class acts as the arena in which a mission takes place.  On top of being
 * the map, it includes all of the mission dynamics such as unit placements,
 * teamtypes, triggers, factions, etc.<br>
 * <br>
 * This class makes use of the callback interface of {@link GameFlowListener}.
 * Constants used to identify loaded parts are found in this class, prefixed by
 * <code>LOADED_MISSION_</code>.
 * 
 * @author Emanuel Rabina
 */
public abstract class Mission {

	// Singleton mission instance
	private static Mission mission;

	// Mission parts
	protected String introvidname;
	protected String briefingvidname;
	protected String actionvidname;
	protected String themename;

	/**
	 * Constructor, initializes several common parts which subclasses should
	 * fill-in in their constructors.
	 * 
	 * @param missionfile File containing the mission data.
	 * @param callback	  Callback to notify of significant loading events.
	 */
	@SuppressWarnings("unused")
	protected Mission(MissionFile missionfile, MissionCallback callback) {
	}

	/**
	 * Constructs and returns a new mission based on the given mission file.
	 * 
	 * @param missiondesc Descriptor of the mission to load.
	 * @param callback	  Callback to notify of loading events.
	 * @return New <code>Mission</code>, or null if the map file could not be
	 * 		   understood.
	 */
	public static Mission createMission(MissionDescriptor missiondesc, MissionCallback callback) {

		switch (CNCGameTypes.getCurrentType()) {
		case TIBERIUM_DAWN:
//			map = new MapTD(missionfile);
			break;
		case RED_ALERT:
			mission = new MissionRA((IniFile)FileManager.getMissionFile(missiondesc.getFile()), callback);
			break;
		}

		return mission;
	}

	/**
	 * Returns the currently running mission.
	 * 
	 * @return The current mission, or <code>null</code> if there is no current
	 * 		   mission.
	 */
	public static Mission currentMission() {

		return mission;
	}

	/**
	 * Returns the filename of the action video for this mission.
	 * 
	 * @return The action video's filename, or <code>null</code> if there is no
	 * 		   video.
	 */
	public String getActionVideoName() {

		return actionvidname;
	}

	/**
	 * Returns the filename of the briefing video for this mission.
	 * 
	 * @return The briefing video's filename, or <code>null</code> if there is
	 * 		   no video.
	 */
	public String getBriefingVideoName() {

		return briefingvidname;
	}

	/**
	 * Returns the filename of the intro video for this mission.
	 * 
	 * @return The intro video's filename, or <code>null</code> if there is no
	 * 		   video.
	 */
	public String getIntroVideoName() {

		return introvidname;
	}

	/**
	 * Returns the initial music track for this mission.
	 * 
	 * @return The starting track, or <code>null</code> if no track has been
	 * 		   assigned.
	 */
	public SoundTrack getTheme() {

		return themename != null ?
				new SoundTrack(FileManager.getSoundTrackFile(themename)):
				null;
	}
}
