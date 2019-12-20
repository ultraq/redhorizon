
// ====================================================
// Scanner's Java - Specifies the depth of game objects
// ====================================================

package redhorizon.game;

/**
 * Interface of the various z-values of game objects.  Even though the game is
 * 2-dimensional, certain images have to be layered atop others so as not to
 * break the illusion of the game.
 * 
 * @author Emanuel Rabina
 */
public interface GameObjectDepths {

	// Depth at which each map layer should be drawn
	public static final int DEPTH_MAP             = 0;
	public static final int DEPTH_MAP_MAPPACK     = 1;
	public static final int DEPTH_MAP_OVERLAYPACK = 2;
	public static final int DEPTH_MAP_TERRAIN     = 3;

	// Depth of units and structures
//	public static final int DEPTH_UNIT      = 10;
	public static final int DEPTH_INFANTRY  = 10;
	public static final int DEPTH_VEHICLE   = 11;
	public static final int DEPTH_STRUCTURE = 12;

	// Depth of HUD/EVA elements
	public static final int DEPTH_HUD               = 20;
	public static final int DEPTH_HUD_STATUS_BOX    = 21;
	public static final int DEPTH_HUD_SELECTION_BOX = 22;

	// Depth of viewable media
	public static final int DEPTH_MEDIA               = 70;
	public static final int DEPTH_MEDIA_LOADINGSCREEN = 71;
	public static final int DEPTH_MEDIA_VIDEO         = 72;

	// Depth of overlays and debug elements?
	public static final int DEPTH_OVERLAY_MAP    = 90;
	public static final int DEPTH_OVERLAY_BOUNDS = 91;
	public static final int DEPTH_OVERLAY_INFO   = 92;
}
