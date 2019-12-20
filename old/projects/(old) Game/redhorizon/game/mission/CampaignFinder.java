
// =======================================
// Scanner's Java - Campaign & Mod manager
// =======================================

package redhorizon.game.mission;

import redhorizon.launcher.LauncherSettingsKeys;
import redhorizon.misc.CNCGameTypes;
import redhorizon.settings.GameData;
import redhorizon.settings.Settings;
import redhorizon.strings.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class which discovers available campaigns/mods so as to make smaller
 * descriptions of them available for selection and play from the game menus.
 * 
 * @author Emanuel Rabina
 */
public class CampaignFinder {

	// List of campaign descriptions
	private static LinkedHashMap<CNCGameTypes,ArrayList<CampaignDescriptor>> campaigndescriptions;
	static {
		campaigndescriptions = new LinkedHashMap<CNCGameTypes,ArrayList<CampaignDescriptor>>();
		for (CNCGameTypes gametype: CNCGameTypes.values()) {
			campaigndescriptions.put(gametype, new ArrayList<CampaignDescriptor>());
		}
	}

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private CampaignFinder() {
	}

	/**
	 * Creates and stores a {@link CampaignDescriptor} for the given parameters.
	 * 
	 * @param gametype	  The game this campaign should be played with.
	 * @param side		  Side this campaign will belong to.
	 * @param description Description of the campaign.
	 */
	private static void createCampaignData(CNCGameTypes gametype, CNCMissionsStringsKeys side,
		CNCMissionsStringsKeys description) {

		CampaignDescriptor campaigndesc = new CampaignDescriptor(
				Strings.getText(side, gametype.name),
				Strings.getText(description));

		// Load save data on this campaign (if available)
		if (GameData.hasCampaignData(campaigndesc)) {
			List<String> missionlist = GameData.getCampaignData(campaigndesc);

			// Load (locale-specific) mission descriptors for each unlocked mission
			for (String filename: missionlist) {
				String missionname = new String();

				switch (gametype) {
				case RED_ALERT:
					missionname = Strings.getText(CNCMissionsStringsKeys.valueOf(
							side.name().contains("ALLIES") ? "MISSION_RA_ALLIES_" : "MISSION_RA_SOVIET_" +
							filename.substring(3)));
					break;
				case TIBERIUM_DAWN:
					missionname = Strings.getText(CNCMissionsStringsKeys.valueOf(
							side.name().contains("GDI") ? "MISSION_TD_GDI_" : "MISSION_TD_NOD_" +
							filename.substring(3)));
					break;
				}

				campaigndesc.addMissionDescriptor(new MissionDescriptor(
						filename, missionname));
			}
		}
		// Otherwise create the default 'new game' option
		else {
			switch (gametype) {
			case RED_ALERT:
				campaigndesc.addMissionDescriptor(new MissionDescriptor(
						side.name().contains("ALLIES") ? "Scg01ea" : "Scu01ea",
						Strings.getText(CNCMissionsStringsKeys.GAMENEW_NEWGAME)));
				break;
			case TIBERIUM_DAWN:
				campaigndesc.addMissionDescriptor(new MissionDescriptor(
						side.name().contains("GDI") ? "Scg01ea" : "Scb01ea",
						Strings.getText(CNCMissionsStringsKeys.GAMENEW_NEWGAME)));
				break;
			}
		}
		campaigndescriptions.get(gametype).add(campaigndesc);
	}

	/**
	 * Returns a list of <tt>CampaignDescriptor</tt>s for each of the campaigns
	 * found by this campaign manager.
	 * 
	 * @return List of campaign description data, arranged by {@link CNCGameTypes}.
	 */
	public static Map<CNCGameTypes,ArrayList<CampaignDescriptor>> getCampaignDescriptors() {

		return campaigndescriptions;
	}

	/**
	 * Locates all available campaigns installed within the game's directory
	 * structure.
	 */
	public static void findCampaigns() {

		// Include RA campaigns
		if (Boolean.parseBoolean(Settings.getSetting(InstalledGamesPreferences.INSTALLED_REDALERT))) {
			createCampaignData(CNCGameTypes.RED_ALERT, CNCMissionsStringsKeys.CAMPAIGN_RA_ALLIES,
					CNCMissionsStringsKeys.DESCRIPTION_RA_ALLIES);
			createCampaignData(CNCGameTypes.RED_ALERT, CNCMissionsStringsKeys.CAMPAIGN_RA_SOVIET,
					CNCMissionsStringsKeys.DESCRIPTION_RA_SOVIET);
		}
		// Include TD campaigns
		if (Boolean.parseBoolean(Settings.getSetting(InstalledGamesPreferences.INSTALLED_TIBERIUMDAWN))) {
			createCampaignData(CNCGameTypes.TIBERIUM_DAWN, CNCMissionsStringsKeys.CAMPAIGN_TD_GDI,
					CNCMissionsStringsKeys.DESCRIPTION_TD_GDI);
			createCampaignData(CNCGameTypes.TIBERIUM_DAWN, CNCMissionsStringsKeys.CAMPAIGN_TD_GDI,
					CNCMissionsStringsKeys.DESCRIPTION_TD_GDI);
		}
	}
}
