
// =========================================
// Scanner's Java - The different game types
// =========================================

package redhorizon.cnc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Representations of the different game types, including full names, acronyms,
 * etc.
 * 
 * @author Emanuel Rabina
 */
@XmlType(name = "CNCGameTypes")
@XmlEnum
public enum CNCGameTypes {

	RED_ALERT     ("Red Alert",     "RedAlert",     "RA"),
	TIBERIUM_DAWN ("Tiberium Dawn", "TiberiumDawn", "TD"),
	RED_HORIZON   ("Red Horizon",   "RedHorizon",   "RH");

	public final String name;
	public final String abbrev;
	public final String acronym;

	/**
	 * Initializes each enumerated type.
	 * 
	 * @param name	  The full name of the game.
	 * @param abbrev  Abbreviated version of the full name.
	 * @param acronym Acronym used to represent the game.
	 */
	private CNCGameTypes(String name, String abbrev, String acronym) {

		this.name    = name;
		this.abbrev  = abbrev;
		this.acronym = acronym;
	}

	/**
	 * Attempts to locate the matching enumerated type label given the match
	 * parameter.
	 * 
	 * @param match The <tt>String</tt> representation of the type's label to
	 * 				match-up.
	 * @return The matching enumerated type.
	 */
	public static CNCGameTypes getMatchingType(String match) {

		for (CNCGameTypes types: CNCGameTypes.values()) {
			if (types.name.equals(match)) {
				return types;
			}
		}
		throw new EnumConstantNotPresentException(CNCGameTypes.class, match);
	}

	public static CNCGameTypes fromValue(String v) {
		return valueOf(v);
	}

	public String value() {
		return name();
	}
}
