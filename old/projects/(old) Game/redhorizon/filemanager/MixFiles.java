
// =======================================
// Scanner's Java - Game startup constants
// =======================================

package redhorizon.filemanager;

import redhorizon.misc.CNCGameTypes;

/**
 * Enumerated types which list the various content files required for the proper
 * running of Red Horizon.  This is the amalgamation of both Tiberium Dawn's and
 * Red Alert's MIX files.
 * 
 * @author Emanuel Rabina
 */
public enum MixFiles {

	// Mix containers
//	GAME_MAIN     ("Main.mix",     "main.mix",     CNCGameTypes.RED_ALERT, false),
//	GAME_REDALERT ("Redalert.mix", "redalert.mix", CNCGameTypes.RED_ALERT, false),

	// C&C Localizations
//	LOCAL_TD   ("Local_TD.mix",   "cclocal.mix", CNCGameTypes.TIBERIUM_DAWN, true),
	LOCAL_RA   ("Local_RA.mix",   "local.mix",   CNCGameTypes.RED_ALERT,     true),
//	TRANSIT_TD ("Transit_TD.mix", "transit.mix", CNCGameTypes.TIBERIUM_DAWN, true),

	// C&C Hires/Lores files
//	HIRES_TD ("Hires_TD.mix", "updatec.mix", CNCGameTypes.TIBERIUM_DAWN, true),
	HIRES_RA ("Hires_RA.mix", "hires.mix",   CNCGameTypes.RED_ALERT,     true),
	LORES_RA ("Lores_RA.mix", "lores.mix",   CNCGameTypes.RED_ALERT,     true),

	// Mission files
//	MISSIONS_TD ("Missions_TD.mix", "general.mix", CNCGameTypes.TIBERIUM_DAWN, true),
	MISSIONS_RA ("Missions_RA.mix", "general.mix", CNCGameTypes.RED_ALERT,     true),

	// Audio files
	COMM_ALLIES ("Comm_Allies.mix", "allies.mix",  CNCGameTypes.RED_ALERT,     true),
	COMM_SOVIET ("Comm_Soviet.mix", "russian.mix", CNCGameTypes.RED_ALERT,     true),
	EVA_RA      ("Eva_RA.mix",      "speech.mix",  CNCGameTypes.RED_ALERT,     true),
//	EVA_TD      ("Eva_TD.mix",      "aud.mix",     CNCGameTypes.TIBERIUM_DAWN, true),
	SCORE_RA    ("Score_RA.mix",    "scores.mix",  CNCGameTypes.RED_ALERT,     true),
//	SCORE_TD    ("Score_TD.mix",    "scores.mix",  CNCGameTypes.TIBERIUM_DAWN, true),
	SOUNDS_RA   ("Sounds_RA.mix",   "sounds.mix",  CNCGameTypes.RED_ALERT,     true),
//	SOUNDS_TD   ("Sounds_TD.mix",   "sounds.mix",  CNCGameTypes.TIBERIUM_DAWN, true),

	// Graphics files
	CONQUER_RA      ("Conquer_RA.mix",      "conquer.mix",  CNCGameTypes.RED_ALERT,     true),
//	CONQUER_TD      ("Conquer_TD.mix",      "conquer.mix",  CNCGameTypes.TIBERIUM_DAWN, true),
//	ICONS_TD        ("Icons_TD.mix",        "tempicnh.mix", CNCGameTypes.TIBERIUM_DAWN, true),
//	TILES_DESERT    ("Tiles_Desert.mix",    "desert.mix",   CNCGameTypes.TIBERIUM_DAWN, true),
	TILES_INTERIOR  ("Tiles_Interior.mix",  "interior.mix", CNCGameTypes.RED_ALERT,     true),
	TILES_SNOW      ("Tiles_Snow.mix",      "snow.mix",     CNCGameTypes.RED_ALERT,     true),
	TILES_TEMPERATE ("Tiles_Temperate.mix", "temperat.mix", CNCGameTypes.RED_ALERT,     true),
//	TILES_WINTER    ("Tiles_Winter.mix",    "winter.mix",   CNCGameTypes.TIBERIUM_DAWN, true),

	// Movie files
	MOVIES1_RA ("Movies_Allies.mix", "movies1.mix", CNCGameTypes.RED_ALERT,     false),
	MOVIES2_RA ("Movies_Soviet.mix", "movies2.mix", CNCGameTypes.RED_ALERT,     false),
	MOVIES1_TD ("Movies_GDI.mix",    "movies.mix",  CNCGameTypes.TIBERIUM_DAWN, false),
	MOVIES2_TD ("Movies_NOD.mix",    "movies.mix",  CNCGameTypes.TIBERIUM_DAWN, false);

	public final String    filename;
	public final String    origname;
	public final CNCGameTypes gametype;
	public final boolean   essential;

	/**
	 * Initializes enumerated types with the proper parameters.
	 * 
	 * @param filename	The name of the file in the Red Horizon directory.
	 * @param original	Name of the original file.
	 * @param gametype	The game which this mix file originally belongs to.
	 * @param essential <code>true</code> if the filename is that of a file
	 * 					required on HDD.  <code>false</code> if it can be
	 * 					spooled from the CD or it's original location.
	 */
	private MixFiles(String filename, String original, CNCGameTypes gametype, boolean essential) {

		this.filename  = filename;
		this.origname  = original;
		this.gametype  = gametype;
		this.essential = essential;
	}
}
