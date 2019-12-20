
// =======================================
// Scanner's Java - New Game menu routines
// =======================================

package redhorizon.launcher;

import redhorizon.game.mission.CampaignDescriptor;
import redhorizon.game.mission.CampaignFinder;
import redhorizon.game.mission.MissionDescriptor;
import redhorizon.misc.CNCGameTypes;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Abstract class definition for the New Game GUI.  Generates campaign and
 * mission information for the implementation class to utilize in it's GUI.
 * 
 * @author Emanuel Rabina
 */
public abstract class NewGameMenu {

	protected LinkedHashMap<CNCGameTypes,ArrayList<CampaignDescriptor>> campaigns =
			new LinkedHashMap<CNCGameTypes,ArrayList<CampaignDescriptor>>();
	protected boolean exitcode;

	/**
	 * Constructor, retrieves a list of the available campaigns from the
	 * {@link CampaignFinder}.
	 */
	protected NewGameMenu() {

		campaigns.putAll(CampaignFinder.getCampaignDescriptors());
	}

	/**
	 * Returns the exit code, used to signify whether or not to close the
	 * top-level menu and continue with the next phase of starting the game.
	 * 
	 * @return <tt>true</tt> if the program should continue, <tt>false</tt>
	 * 		   otherwise.
	 */
	public boolean exitCode() {

		return exitcode;
	}

	/**
	 * Sets in the {@link GameData} class which campaign/mod to start loading
	 * (and playing?) once the game engine starts.
	 * 
	 * @param campaign Descriptor for the campaign to play.
	 */
	protected void setGameToPlay(CampaignDescriptor campaign) {

		GameData.setNextGame(campaign);
		CNCGameTypes.setCurrentType(gametype);
	}
}
