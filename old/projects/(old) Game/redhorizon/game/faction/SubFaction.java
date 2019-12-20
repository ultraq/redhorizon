
// ========================================
// Scanner's Java - A country (sub-faction)
// ========================================

package redhorizon.game.faction;

import redhorizon.misc.CNCCountryColours;
import redhorizon.xml.factions.XMLSubFaction;

import java.util.ArrayList;

/**
 * Representation of a country (sub-faction), allows for a faction to be
 * composed of parts that contain some special ability over the other
 * subfactions.  While their technology tree is still tied to the overall
 * {@link Faction}, these sub-factions create a greater level of diversity in
 * gameplay.
 * 
 * @author Emanuel Rabina
 */
public class SubFaction {

	// List of subfactions
	private static final ArrayList<SubFaction> subfactions = new ArrayList<SubFaction>();

	// Subfaction attributes
	final String id;
	final Faction parent;
	final CNCCountryColours colour;

	/**
	 * Package-visible constructor, creates a new <code>SubFaction</code> from
	 * the subfaction XML data.
	 * 
	 * @param subfaction Subfaction data from the XML object.
	 * @param parent	 Parent faction this subfaction belongs to.
	 */
	SubFaction(XMLSubFaction subfaction, Faction parent) {

		this.id     = subfaction.getID();
		this.parent = parent;
		this.colour = CNCCountryColours.getMatchingType(subfaction.getColour().name());

		subfactions.add(this);
	}

	/**
	 * Returns a subfaction with the matching name.
	 * 
	 * @param subfactionid ID of the subfaction to retrieve.
	 * @return Matching <code>SubFaction</code>, or <code>null</code> if that
	 * 		   subfaction doesn't exist.
	 */
	public static SubFaction getSubFactionByID(String subfactionid) {

		for (SubFaction subfaction: subfactions) {
			if (subfaction.id.equals(subfactionid)) {
				return subfaction;
			}
		}
		return null;
	}

	/**
	 * Returns the colour/palette shift associated with this subfaction.
	 * 
	 * @return Palette shift data.
	 */
	public CNCCountryColours getColour() {

		return colour;
	}

	/**
	 * Returns the parent faction of this subfaction.
	 * 
	 * @return Parent faction.
	 */
	public Faction getFaction() {

		return parent;
	}

	/**
	 * Returns this subfaction's ID.
	 * 
	 * @return Subfaction ID.
	 */
	public String getID() {

		return id;
	}
}
