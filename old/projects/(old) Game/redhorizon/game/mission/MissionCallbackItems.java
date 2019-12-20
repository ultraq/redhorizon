
// ======================================
// Scanner's Java - Mission loading items
// ======================================

package redhorizon.game.mission;

/**
 * Enumeration of the bits of a mission that will cause a callback event when
 * they have completed loading.
 * 
 * <p>For media such as videos and theme music, the filename of the media will
 * also be passed to the callback in the <code>details</code> parameter.
 * 
 * @author Emanuel Rabina
 */
public enum MissionCallbackItems {

	// Videos
	MISSION_VIDEO_INTRO,
	MISSION_VIDEO_BRIEFING,
	MISSION_VIDEO_ACTION,

	// Starting music
	MISSION_THEME;
}
