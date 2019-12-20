
// ========================================
// Scanner's Java - Parent unit abstraction
// ========================================

package redhorizon.game.objects.units;

import redhorizon.game.Rules;
import redhorizon.game.RulesException;
import redhorizon.game.RulesStringsKeys;
import redhorizon.game.faction.SubFaction;
import redhorizon.game.objects.Implementation;
import redhorizon.misc.geometry.Rectangle2D;
import redhorizon.strings.Strings;
import redhorizon.xml.units.XMLUnit;

import java.lang.reflect.Method;

/**
 * Top-level abstraction of the Red Horizon unit type.  The implementation is
 * the one that all instances draw their attributes from, much like the
 * blueprint for this sort of unit.<br>
 * <br>
 * This class also encompasses much of the Structure type.  Both units and
 * structures share a lot of similarities, and due to the use of the word 'unit'
 * to include structures in the same group as other units in the original Red
 * Alert configuration (<code>Rules.ini</code>), this class will remain the
 * parent abstraction for both types.
 * 
 * @author Emanuel Rabina
 * @param <N> Unit instance that this implementation can create.
 */
public abstract class UnitImpl<N extends Unit<? extends UnitImpl<N>>> implements Implementation<N> {

	// Unit attributes
	protected final SubFaction  subfaction;
	protected final String      name;
	protected final int         maxhp;
	protected final int         maxspeed;
	protected final Rectangle2D footprint;

	/**
	 * Constructor, builds a unit implementation for this faction from the given
	 * configuration settings.  The given specifications also will have all
	 * their attributes filled-in by any specified templates.
	 * 
	 * @param subfaction Subfaction this implementation belongs to.
	 * @param specs		 XML of unit attributes and settings.
	 */
	public UnitImpl(SubFaction subfaction, XMLUnit specs) {

		this.subfaction = subfaction;
		this.name       = specs.getID();

		// Unit template of defaults
		completeDefinition(specs);

		// Unit attributes
		this.maxhp    = specs.getHitpoints();
		this.maxspeed = specs.getSpeed();

		if (!specs.isSetFootprint()) {
			throw new RulesException(Strings.getText(RulesStringsKeys.MISSING_DATA, "footprint", name));
		}
		this.footprint = specs.getFootprint();
	}

	/**
	 * Recursively fills-in the blanks of the given specs so that any empty
	 * entries are completed by what is specified in the specs' template (if
	 * any).
	 * 
	 * @param specs XML specifications to fill.
	 */
	@SuppressWarnings("unchecked")
	private static void completeDefinition(XMLUnit specs) {

		if (!specs.isSetTemplate()) {
			return;
		}

		// Get the template for these specs
		XMLUnit template = Rules.currentRules().getUnitByID(specs.getTemplate());
		completeDefinition(template);

		// Override where necessary
		Method[] getmethods = specs.getClass().getMethods();
		for (Method getmethod: getmethods) {
			String getmethodname = getmethod.getName();

			// Single attribute override
			if (getmethodname.startsWith("get")) {
				Method getmethodtemp = template.getClass().getMethod(getmethodname);
				if (getmethod.invoke(specs) == null) {
					Method setmethod = specs.getClass().getMethod("set" +
							getmethodname.substring(3), getmethod.getReturnType());
					setmethod.invoke(specs, getmethodtemp.invoke(template));
				}
			}
		}

		// List (multiple attribute) appends/removes
		specs.getCharacteristics().addAll(template.getCharacteristics());
		specs.getCharacteristics().removeAll(specs.getUnCharacteristics());
	}
}
