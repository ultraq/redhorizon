
// =====================================
// Scanner's Java - Campaign description
// =====================================

package redhorizon.game.mission;

import redhorizon.misc.CNCGameTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Campaign or mod information object, used to hold basic campaign/mod
 * information which can be read or from which a new campaign can be initiated.
 * 
 * @author Emanuel Rabina
 */
public class CampaignDescriptor implements Serializable {

	private final CNCGameTypes gametype;
	private final String name;
	private final String description;
	private final ArrayList<MissionDescriptor> missions = new ArrayList<MissionDescriptor>();

	/**
	 * Constructor, sets this campaign's name and description.
	 * 
	 * @param gametype	  The type of game required to play the campaign.
	 * @param name		  Name of the campaign.
	 * @param description Description of the campaign.
	 */
	CampaignDescriptor(CNCGameTypes gametype, String name, String description) {

		this.gametype    = gametype;
		this.name        = name;
		this.description = description;
	}

	/**
	 * Adds a <tt>MissionDescriptor</tt> to the list of missions for this
	 * campaign.
	 * 
	 * @param mission Mission to associate with this campaign.
	 */
	void addMissionDescriptor(MissionDescriptor mission) {

		missions.add(mission);
	}

	/**
	 * Returns the description for this campaign.
	 * 
	 * @return Campaign's descriptive text.
	 */
	public String getDescription() {

		return description;
	}

	/**
	 * Returns the game type required to play this campaign.
	 * 
	 * @return The <tt>CNCGameTypes</tt> required to play this campaign.
	 */
	public CNCGameTypes getGameType() {

		return gametype;
	}

	/**
	 * Returns the <tt>MissionDescriptor</tt> with the given name.
	 * 
	 * @param missionname Name of the mission to find.
	 * @return <tt>MissionDescriptor</tt> with the matching name, <tt>null</tt>
	 * 		   if it doesn't exist.
	 */
	public MissionDescriptor getMissionDescriptor(String missionname) {

		for (MissionDescriptor mission: missions) {
			if (missionname.equals(mission.getName())) {
				return mission;
			}
		}
		return null;
	}

	/**
	 * Returns the list of <tt>MissionDescriptor</tt>s for this campaign.
	 * 
	 * @return List of missions for this campaign.
	 */
	public List<MissionDescriptor> getMissionDescriptors() {

		return missions;
	}

	/**
	 * Returns the name of the campaign.
	 * 
	 * @return Campaign name.
	 */
	public String getName() {

		return name;
	}
}
