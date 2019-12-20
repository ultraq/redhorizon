
// =====================================
// Scanner's Java - Campaign description
// =====================================

package redhorizon.game.mission;

import redhorizon.settings.GameData;

/**
 * Data structure for a campaign, it's accompanying description, and list of
 * missions.  The campaign is the overall container of it's own mission
 * structure, along with missions that form the campaign.  A player's progress
 * can also be stored and retrieved from a campaign.
 * 
 * @author Emanuel Rabina
 */
public class Campaign {

	// Campaign attributes
//	private final CampaignDescriptor descriptor;

	/**
	 * Constructor, creates a new campaign using the data in the descriptor.
	 * 
	 * @param descriptor Description of the campaign.
	 */
	Campaign(CampaignDescriptor descriptor) {

//		this.descriptor = descriptor;
	}

	/**
	 * Creates and returns a new campaign based on the given campaign
	 * description.  Quite often this is the one that the user last selected
	 * (and saved) to the {@link GameData} class.
	 * 
	 * @param descriptor <code>CampaignDescriptor</code> for the campaign to
	 * 					 create. 
	 * @return Newly created campaign.
	 */
	public static Campaign createNewCampaign(CampaignDescriptor descriptor) {

		return new Campaign(descriptor);
	}
}
