
// ============================================
// Scanner's Java - Representation of a faction
// ============================================

package redhorizon.game.faction;

import redhorizon.game.Rules;
import redhorizon.xml.factions.XMLFaction;
import redhorizon.xml.factions.XMLSubFaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a side/faction in the game.  The primary use of this class
 * is to maintain references to faction-specific data, like the list of
 * sub-factions, it's technology tree, etc.
 * 
 * @author Emanuel Rabina
 */
public class Faction {

	// List of factions
	private static final ArrayList<Faction> factions = new ArrayList<Faction>();

	// Faction attributes
	private final String id;
	private final ArrayList<SubFaction> subfactions = new ArrayList<SubFaction>();

	/**
	 * Hidden constructor, creates a new faction using the given faction data.
	 * 
	 * @param faction Faction data from the XML object.
	 */
	private Faction(XMLFaction faction) {

		this.id = faction.getID();

		// Add each new subfaction
		List<XMLSubFaction> subfactionlist = faction.getSubFaction();
		for (XMLSubFaction subfaction: subfactionlist) {
			subfactions.add(new SubFaction(subfaction, this));
		}
	}

	/**
	 * Creates all of the known factions from the base <code>Rules</code> file.
	 */
	public static void createFactions() {

		// Create new factions
		for (XMLFaction faction: Rules.currentRules().getFactions()) {
			factions.add(new Faction(faction));
		}
	}

	/**
	 * Returns the faction with the given name.
	 * 
	 * @param factionid ID of the faction to retrieve.
	 * @return The <code>Faction</code> with that name, or <code>null</code> if
	 * 		   that faction can't be found.
	 */
	public static Faction getFactionByID(String factionid) {

		for (Faction faction: factions) {
			if (faction.id.equals(factionid)) {
				return faction;
			}
		}
		return null;
	}

	/**
	 * Returns this faction's ID string.
	 * 
	 * @return faction's ID string.
	 */
	public String getID() {

		return id;
	}

	/**
	 * Retrieves a subfaction from this parent faction.
	 * 
	 * @param subfactionid ID of the subfaction to retrieve.
	 * @return The <code>SubFaction</code> with that ID, or <code>null</code>
	 * 		   if that subfaction can't be found.
	 */
	public SubFaction getSubFactionByID(String subfactionid) {

		for (SubFaction subfaction: subfactions) {
			if (subfaction.id.equals(subfactionid)) {
				return subfaction;
			}
		}
		return null;
	}
}
