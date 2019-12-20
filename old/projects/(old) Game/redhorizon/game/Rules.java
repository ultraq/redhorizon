
// ====================================
// Scanner's Java - Game rules/settings
// ====================================

package redhorizon.game;

import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.File;
import redhorizon.filetypes.TextFileBridge;
import redhorizon.misc.CNCGameTypes;
import redhorizon.strings.Strings;
import redhorizon.xml.XMLInclude;
import redhorizon.xml.XMLIncludes;
import redhorizon.xml.XMLRules;
import redhorizon.xml.factions.XMLFaction;
import redhorizon.xml.factions.XMLFactionData;
import redhorizon.xml.media.XMLSoundTrack;
import redhorizon.xml.soundtrack.XMLSoundtrackData;
import redhorizon.xml.units.XMLStructure;
import redhorizon.xml.units.XMLStructureData;
import redhorizon.xml.units.XMLUnit;
import redhorizon.xml.units.XMLUnitData;

import nz.net.ultraq.common.xmlutils.XMLReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Class which contains all of the settings and attributes of in-game objects
 * and behaviours.  The <code>Rules</code> class is first created with what is
 * deemed as a 'default' set of rules.<br>
 * <br>
 * It starts with the base <code>Rules.xml</code> file, loading on top of that
 * the classic <code>Rules.ini</code> if it exists somewhere on the mod path.
 * These 2 combined form the 'default' rules which can be overriden on a
 * per-mission basis and returned to normal using {@link #revertToDefault()}.
 * 
 * @author Emanuel Rabina
 */
public abstract class Rules {

	// Default rules constants
	private static final String RULES_BASE    = "Rules.xml";
	private static final String RULES_CONTEXT = "redhorizon.xml";

	private static final String CONTEXT_SOUNDTRACK = RULES_CONTEXT + ".soundtrack";
	private static final String CONTEXT_FACTIONS   = RULES_CONTEXT + ".factions";
	private static final String CONTEXT_UNITS      = RULES_CONTEXT + ".units";

	// Singleton Rules instance
	private static Rules rules;

	// Default rules set
	protected final XMLRules rulesdata;

	protected final HashMap<String,XMLSoundTrack> soundtrack = new HashMap<String,XMLSoundTrack>();
	protected final HashMap<String,XMLFaction> factions = new HashMap<String,XMLFaction>();
	protected final HashMap<String,XMLUnit> units = new HashMap<String,XMLUnit>();

	/**
	 * Constructor, sets the base defaults used across Red Horizon.
	 */
	protected Rules() {

		// Load defaults from XML file
		TextFileBridge rulesfile = FileManager.getTextFileBridge(RULES_BASE);
		RulesSchemaResolver resolver = new RulesSchemaResolver();

		XMLReader<XMLRules> xmlfactory = new XMLReader<XMLRules>(
				rulesfile.asBufferedReader(), RULES_CONTEXT, true, resolver);
		rulesdata = xmlfactory.getXMLData();

		// Catalogue all other section data

		ArrayList<XMLSoundtrackData> soundtrackdatas = parseXMLData(
				rulesdata.getSoundtrack(), CONTEXT_SOUNDTRACK, resolver);
		for (XMLSoundtrackData soundtrackdata: soundtrackdatas) {
			for (XMLSoundTrack soundtrackd: soundtrackdata.getSoundtrack()) {
				soundtrack.put(soundtrackd.getID(), soundtrackd);
			}
		}

		ArrayList<XMLFactionData> factiondatas = parseXMLData(
				rulesdata.getFactions(), CONTEXT_FACTIONS, resolver);
		for (XMLFactionData factiondata: factiondatas) {
			for (XMLFaction factiond: factiondata.getFaction()) {
				factions.put(factiond.getID(), factiond);
			}
		}

		ArrayList<XMLUnitData> unitdatas = parseXMLData(
				rulesdata.getUnits(), CONTEXT_UNITS, resolver);
		for (XMLUnitData unitdata: unitdatas) {
			for (XMLUnit unitd: unitdata.getInfantryOrVehicle()) {
				units.put(unitd.getID(), unitd);
			}
		}

		ArrayList<XMLStructureData> structuredatas = parseXMLData(
				rulesdata.getStructures(), CONTEXT_UNITS, resolver);
		for (XMLStructureData structuredata: structuredatas) {
			for (XMLStructure structured: structuredata.getStructure()) {
				units.put(structured.getID(), structured);
			}
		}
	}

	/**
	 * Initializes this class for the current gametype's rule set, which further
	 * mod-specific files can override.
	 * 
	 * @return Newly created set of rules.
	 */
	public static Rules createRules() {

		switch (CNCGameTypes.getCurrentType()) {
		case TIBERIUM_DAWN:
//			rules = new RulesTD();
			break;
		case RED_ALERT:
			rules = new RulesRA();
			break;
		}

		return rules;
	}

	/**
	 * Returns the current instance of the <code>Rules</code>.
	 * 
	 * @return Current instance of <code>Rules</code>, or <code>null</code> if
	 * 		   none exists.
	 */
	public static Rules currentRules() {

		return rules;
	}

	/**
	 * Returns the <code>Faction</code> data for the faction with the given ID.
	 * 
	 * @param id Faction ID string.
	 * @return Faction XML data.
	 * @throws RulesException If the faction with the given ID
	 * 		   doesn't exist.
	 */
	public XMLFaction getFactionByID(String id) throws RulesException {

		if (!factions.containsKey(id)) {
			throw new RulesException(Strings.getText(RulesStringsKeys.UNKNOWN_FACTION, id));
		}
		return factions.get(id);
	}

	/**
	 * Returns the collection of <code>Faction</code> data.
	 * 
	 * @return Faction collection.
	 */
	public Collection<XMLFaction> getFactions() {

		return factions.values();
	}

	/**
	 * Returns the collection of <code>Soundtrack</code> data.
	 * 
	 * @return Soundtrack collection.
	 */
	public Collection<XMLSoundTrack> getSoundtrack() {

		return soundtrack.values();
	}

	/**
	 * Returns the <code>Unit</code> data for the unit with the given ID.
	 * 
	 * @param id Unit ID string.
	 * @return Unit XML data.
	 * @throws RulesException If the unit/structure with the given
	 * 		   ID doesn't exist.
	 */
	public XMLUnit getUnitByID(String id) throws RulesException {

		if (!units.containsKey(id)) {
			throw new RulesException(Strings.getText(RulesStringsKeys.UNKNOWN_UNIT, id));
		}
		return units.get(id);
	}

	/**
	 * Returns the collection of <code>Unit</code> data.
	 * 
	 * @return Unit collection.
	 */
	public Collection<XMLUnit> getUnits() {

		return units.values();
	}

	/**
	 * Takes the values from the given settings file to be used in place of the
	 * values initially loaded.  Only already-existing values are used.
	 * Non-rules entries are ignored.
	 * 
	 * @param overridefile File containing override values.
	 */
	public abstract void overrideDefaults(File overridefile);

	/**
	 * Loads the XML data of an XML file into the given list.
	 * 
	 * @param includes <code>XMLIncludes</code> list of included files.
	 * @param context  Package containing mapped XML data.
	 * @param resolver Custom entity resolver.
	 * @param <X>	   Returned XML data type.
	 * @return List of data surrounding the XML data type.
	 */
	private static <X> ArrayList<X> parseXMLData(XMLIncludes includes, String context,
		RulesSchemaResolver resolver) {

		ArrayList<X> xmldata = new ArrayList<X>();

		for (XMLInclude include: includes.getInclude()) {
			String source = include.getSource();
			XMLReader<X> xmlfactory = new XMLReader<X>(source.contains("/") ?
					new FileReader(FileManager.resourceDirectory() + "/" + source) :
					FileManager.getTextFileBridge(source).asBufferedReader(),
					context, true, resolver);
			xmldata.add(xmlfactory.getXMLData());
		}

		return xmldata;
	}

	/**
	 * Reverts to the default settings by clearing the overridden settings.
	 */
	public abstract void revertToDefault();

	/**
	 * Custom entity resolver class for re-routing schema lookups to the right
	 * place.
	 */
	private static class RulesSchemaResolver implements EntityResolver {

		private static final String SCHEMA_DIR = "Schema";

		/**
		 * Returns an <code>InputSource</code> of the XML schema.
		 * 
		 * @param publicid
		 * @param systemid
		 * @return XML Schema.
		 * @throws IOException If the schema cannot be located.
		 */
		public InputSource resolveEntity(String publicid, String systemid)
			throws IOException {

			String schemaname = systemid.substring(systemid.lastIndexOf('/'));
			String schemapath = SCHEMA_DIR + schemaname;
			return new InputSource(new FileInputStream(schemapath));
		}
	}
}
