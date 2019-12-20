
// =========================================
// Scanner's Java - Red Alert structure line
// =========================================

package redhorizon.game.map;

/**
 * Red Alert specific map structure descriptor.  Expands on the C&C one to
 * include AI repair/rebuild information.
 * 
 * @author Emanuel Rabina
 */
public class StructureDescriptorRA extends StructureDescriptorCNC {

	private static final String TRUE = "1";

	private final boolean repairable;
	private final boolean sellable;

	/**
	 * Constructor, creates a new initial structure description.
	 * 
	 * @param map	The map this structure belongs to.
	 * @param specs Line of text from the <code>[Structure]</code> section of a
	 * 				C&C map file.
	 */
	StructureDescriptorRA(Map map, String[] specs) {

		super(map, specs);

		repairable = specs[7].equals(TRUE);
		sellable   = specs[6].equals(TRUE);
	}

	/**
	 * Returns whether or not the AI is permitted to repair/rebuild this
	 * structure.
	 * 
	 * @return <code>true</code> if the AI should repair the structure,
	 * 		   <code>false</code> otherwise.
	 */
	public boolean isRepairable() {

		return repairable;
	}

	/**
	 * Returns whether or not the AI is permitted to sell the structure when it
	 * is heavily damaged.
	 * 
	 * @return <code>true</code> if the AI can sell this structure in an
	 * 		   emergency, <code>false</code> otherwise.
	 */
	public boolean isSellable() {

		return sellable;
	}
}
