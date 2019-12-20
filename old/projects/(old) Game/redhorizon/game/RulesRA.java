
// ================================
// Scanner's Java - Red Alert rules
// ================================

package redhorizon.game;

import redhorizon.filetypes.File;
import redhorizon.filetypes.ini.IniFile;
import redhorizon.strings.Strings;
import redhorizon.xml.units.XMLInfantry;
import redhorizon.xml.units.XMLUnit;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Red Alert specific implementation of the <code>Rules</code> class.  Loads the
 * <code>Rules.ini</code> file and uses it's settings to override the base
 * settings, acting as the complete 'default' rules for the Red Alert game type.
 * 
 * @author Emanuel Rabina
 */
public class RulesRA extends Rules {

	/**
	 * Enumeration of all the Red Alert Rules.ini sections which can be used to
	 * override the default rules.
	 */
	private enum RulesRASections {

		//General settings
		GENERAL        ("General"),
		THEMECONTROL   ("ThemeControl"),
		MULTIPLAYER    ("MultiplayerDefaults"),
		SPECIALWEAPONS ("Recharge"),
//		MAXIMUMS       ("Maximums"),
		AI             ("AI"),
		IQ             ("IQ"),

		//Country settings
		COUNTRY_SPAIN   ("Spain"),   COUNTRY_GREECE  ("Greece"),  COUNTRY_USSR    ("USSR"),
		COUNTRY_ENGLAND ("England"), COUNTRY_UKRAINE ("Ukraine"), COUNTRY_GERMANY ("Germany"),
		COUNTRY_FRANCE  ("France"),  COUNTRY_TURKEY  ("Turkey"),

		// Difficulty settings
//		DIFFICULTY_EASY   ("Easy"),
//		DIFFICULTY_NORMAL ("Normal"),
//		DIFFICULTY_HARD   ("Difficult"),

		// Units

		UNIT_INF_CIVILIAN1  ("C1"),  UNIT_INF_CIVILIAN2 ("C2"),   UNIT_INF_CIVILIAN3  ("C3"),
		UNIT_INF_CIVILIAN4  ("C4"),  UNIT_INF_CIVILIAN5 ("C5"),   UNIT_INF_CIVILIAN6  ("C6"),
		UNIT_INF_CIVILIAN7  ("C7"),  UNIT_INF_CIVILIAN8 ("C8"),   UNIT_INF_CIVILIAN9  ("C9"),
		UNIT_INF_CIVILIAN10 ("C10"), UNIT_INF_CHAN      ("CHAN"), UNIT_INF_DELPHI     ("DELPHI"),
		UNIT_INF_EINSTEIN   ("EINSTEIN"),

		UNIT_INF_ATTACKDOG     ("DOG"), UNIT_INF_RIFLEMAN     ("E1"),   UNIT_INF_GRENADIER ("E2"),
		UNIT_INF_ROCKETSOLDIER ("E3"),  UNIT_INF_FLAMETHROWER ("E4"),   UNIT_INF_ENGINEER  ("E6"),
		UNIT_INF_TANYA         ("E7"),  UNIT_INF_GENERAL      ("GNRL"), UNIT_INF_MEDIC    ("MEDI"),
		UNIT_INF_SPY           ("SPY"), UNIT_INF_THIEF        ("THF"),

		UNIT_VEH_LIGHTTANK   ("1TNK"), UNIT_VEH_MEDIUMTANK       ("2TNK"), UNIT_VEH_HEAVYTANK ("3TNK"),
		UNIT_VEH_MAMMOTHTANK ("4TNK"), UNIT_VEH_APC              ("APC"),  UNIT_VEH_ARTILLERY ("ARTY"),
		UNIT_VEH_CONVOYTRUCK ("TRUK"), UNIT_VEH_HARVESTER        ("HARV"), UNIT_VEH_JEEP      ("JEEP"),
		UNIT_VEH_MCV         ("MCV"),  UNIT_VEH_GAPGENERATOR     ("MGG"),  UNIT_VEH_MINELAYER ("MNLY"),
		UNIT_VEH_RADARJAMMER ("MRJ"),  UNIT_VEH_V2ROCKETLAUNCHER ("V2RL"),

		UNIT_AIR_BADGERBOMBER ("BADR"), UNIT_AIR_APACHE  ("HELI"), UNIT_AIR_HIND     ("HIND"),
		UNIT_AIR_MIG          ("MIG"),  UNIT_AIR_CHINOOK ("TRAN"), UNIT_AIR_SPYPLANE ("U2"),
		UNIT_AIR_YAK          ("YAK"),

		UNIT_SHP_CRUISER   ("CA"), UNIT_SHP_DESTROYER ("DD"), UNIT_SHP_TRANSPORT ("LST"),
		UNIT_SHP_GUNBOAT   ("PT"), UNIT_SHP_SUBMARINE ("SS"),

		// Structures

		BLDG_AFLD             ("AFLD"), BLDG_AAGUN            ("AGUN"), BLDG_ADVANCEDPOWERPLANT ("APWR"),
		BLDG_ALLIEDTECHCENTER ("ATEK"), BLDG_SOVIETBARRACKS   ("BARR"), BLDG_BIORESEARCHLAB     ("BIO"),
		BLDG_RADARDOME        ("DOME"), BLDG_CONSTRUCTIONYARD ("FACT"), BLDG_COMMANDCENTER      ("FCOM"),
		BLDG_SERVICEDEPOT     ("FIX"),  BLDG_FLAMETURRET      ("FTUR"), BLDG_GAPGENERATOR       ("GAP"),
		BLDG_TURRET           ("GUN"),  BLDG_CAMOPILLBOX      ("HBOX"), BLDG_HOSPITAL           ("HOSP"),
		BLDG_HELIPAD          ("HPAD"), BLDG_IRONCURTAIN      ("IRON"), BLDG_KENNEL             ("KENN"),
		BLDG_PRISON           ("MISS"), BLDG_MISSILESILO      ("MSLO"), BLDG_PILLBOX            ("PBOX"),
		BLDG_CHRONOSPHERE     ("PDOX"), BLDG_POWERPLANT       ("POWR"), BLDG_REFINERY           ("PROC"),
		BLDG_SAMSITE          ("SAM"),  BLDG_SILO             ("SILO"), BLDG_SOVIETTECHCENTER   ("STEK"),
		BLDG_SHIPYARD         ("SYRD"), BLDG_SUBPEN           ("SPEN"), BLDG_TENTBARRACKS       ("TENT"),
		BLDG_TESLACOIL        ("TSLA"), BLDG_WEAPONSFACTORY   ("WEAP"),

		BLDG_BARREL ("BARL"), BLDG_BARRELS ("BRL3"),

		BLDG_CIVILIAN1  ("V01"), BLDG_CIVILIAN2  ("V02"), BLDG_CIVILIAN3  ("V03"),
		BLDG_CIVILIAN4  ("V04"), BLDG_CIVILIAN5  ("V05"), BLDG_CIVILIAN6  ("V06"),
		BLDG_CIVILIAN7  ("V07"), BLDG_CIVILIAN8  ("V08"), BLDG_CIVILIAN9  ("V09"),
		BLDG_CIVILIAN10 ("V10"), BLDG_CIVILIAN11 ("V11"), BLDG_CIVILIAN12 ("V12"),
		BLDG_CIVILIAN13 ("V13"), BLDG_CIVILIAN14 ("V14"), BLDG_CIVILIAN15 ("V15"),
		BLDG_CIVILIAN16 ("V16"), BLDG_CIVILIAN17 ("V17"), BLDG_CIVILIAN18 ("V18"),
		BLDG_CIVILIAN19 ("V19"), BLDG_CIVILIAN20 ("V20"), BLDG_CIVILIAN21 ("V21"),
		BLDG_CIVILIAN22 ("V22"), BLDG_CIVILIAN23 ("V23"), BLDG_CIVILIAN24 ("V24"),
		BLDG_CIVILIAN25 ("V25"), BLDG_CIVILIAN26 ("V26"), BLDG_CIVILIAN27 ("V27"),
		BLDG_CIVILIAN28 ("V28"), BLDG_CIVILIAN29 ("V29"), BLDG_CIVILIAN30 ("V30"),
		BLDG_CIVILIAN31 ("V31"), BLDG_CIVILIAN32 ("V32"), BLDG_CIVILIAN33 ("V33"),
		BLDG_CIVILIAN34 ("V34"), BLDG_CIVILIAN35 ("V35"), BLDG_CIVILIAN36 ("V36"),
		BLDG_CIVILIAN37 ("V37"),

		FAKE_CONSTRUCTIONYARD ("FACF"), FAKE_RADARDOME      ("DOMF"), FAKE_SUBPEN ("SPEF"),
		FAKE_SHIPYARD         ("SYRD"), FAKE_WEAPONSFACTORY ("WEAF"),

		MINE_ANTIVEHICLE ("MINV"), MINE_ANTIPERSONNEL ("MINP"),

		WALL_BARBEDWIRE     ("BARB"), WALL_CONCRETE ("BRIK"), WALL_CHAINLINK ("CYCL"),
		WALL_BARBEDWIRECOIL ("FENC"), WALL_SANDBAGS ("SBAG"), WALL_WOOD      ("WOOD"),

		// Weapons
		WEAP_2INCH            ("2Inch"),    WEAP_8INCH           ("8Inch"),
		WEAP_ANTIARMOURSMALL  ("75mm"),     WEAP_ANTIARMOURLIGHT ("90mm"),
		WEAP_ANTIARMOURMEDIUM ("105mm"),    WEAP_ANTIARMOURLARGE ("120mm"),
		WEAP_ARTILLERY        ("155mm"),    WEAP_CAMERA          ("Camera"),
		WEAP_CHAINGUN         ("ChainGun"), WEAP_COLT45          ("Colt45"),
		WEAP_DRAGONROCKET     ("Dragon"),   WEAP_FIREBALL        ("FireballLauncher"),
		WEAP_FLAMER           ("Flamer"),   WEAP_GRENADE         ("Grenade"),
		WEAP_HELLFIREMISSILE  ("Hellfire"), WEAP_RIFLE           ("M1Carbine"),
		WEAP_M60MACHINEGUN    ("M60mg"),    WEAP_MAMMOTHTUSK     ("MammothTusk"),
		WEAP_MAVERICK         ("Maverick"), WEAP_NAPALM          ("Napalm"),
		WEAP_SAMMISSILE       ("Nike"),     WEAP_PISTOL          ("Pistol"),
		WEAP_REDEYE           ("RedEye"),   WEAP_SNIPER          ("Sniper"),
		WEAP_STINGER          ("Stinger"),  WEAP_TESLACOIL       ("TeslaZap"),
		WEAP_TORPEDO          ("TorpTube"), WEAP_TURRET          ("TurretGun"),
		WEAP_VULCAN           ("Vulcan"),   WEAP_FLAK            ("ZSU-23");

		private final String sectionname;

		/**
		 * Constructor, pairs the enum with the section name.
		 * 
		 * @param sectionname Name of the section in the Rules.ini file.
		 */
		private RulesRASections(String sectionname) {

			this.sectionname = sectionname;
		}

		/**
		 * Locates the enum with the given section name.
		 * 
		 * @param sectionname Name of the section.
		 * @return Enum with that section name.
		 */
		private static RulesRASections getMatchingType(String sectionname) {

			for (RulesRASections section: RulesRASections.values()) {
				if (section.sectionname.equals(sectionname)) {
					return section;
				}
			}
			throw new EnumConstantNotPresentException(RulesRASections.class, sectionname);
		}

		/**
		 * Returns whether or not an enum exists for the given section name.
		 * 
		 * @param sectionname Name of the section.
		 * @return <code>true</code> if a matching enum exists,
		 * 		   <code>false</code> otherwise.
		 */
		private static boolean hasMatchingType(String sectionname) {

			for (RulesRASections section: RulesRASections.values()) {
				if (section.sectionname.equals(sectionname)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Enumeration of all the Red Alert unit attributes.
	 */
	private enum RulesRAUnitAttribs {

		UNIT_AMMO      ("Ammo"),
		UNIT_COST      ("Cost"),
		UNIT_HITPOINTS ("Strength"),
		UNIT_POINTS    ("Points"),
		UNIT_SIGHT     ("Sight"),
		UNIT_SPEED     ("Speed"),
		UNIT_TECHLEVEL ("TechLevel");

		private final String attribname;

		/**
		 * Constructor, pairs the enum with the attrib name.
		 * 
		 * @param attribname Name of the attrib from the Rules.ini file.
		 */
		private RulesRAUnitAttribs(String attribname) {

			this.attribname = attribname;
		}

		/**
		 * Returns the matching attribute enum for the attribute name.
		 * 
		 * @param attribname Name of the attribute.
		 * @return Enum with that attribute name.
		 */
		private static RulesRAUnitAttribs getMatchingType(String attribname) {

			for (RulesRAUnitAttribs unitattrib: RulesRAUnitAttribs.values()) {
				if (unitattrib.attribname.equals(attribname)) {
					return unitattrib;
				}
			}
			throw new EnumConstantNotPresentException(RulesRAUnitAttribs.class, attribname);
		}
	}

	// Common enum prefixes/groups
	private static final String PREFIX_FACTION    = "COUNTRY";
	private static final String PREFIX_SOUNDTRACK = "THEMECONTROL";
	private static final String PREFIX_UNIT_INF   = "UNIT_INF";

	// Storage for overriden objects
//	private final HashMap<String,XMLSoundTrack> soundtrackRA = new HashMap<String,XMLSoundTrack>();
//	private final HashMap<String,XMLFaction> factionsRA = new HashMap<String,XMLFaction>();
	private final HashMap<String,XMLUnit> unitsRA = new HashMap<String,XMLUnit>();

	/**
	 * Constructor, loads RA's default rules set, found in the
	 * <code>Rules.ini</code> file.
	 */
	RulesRA() {

		super();
	}

	/**
	 * @inheritDoc
	 * 
	 * TODO: Fill-in overrides for other types
	 */
	public void overrideDefaults(File overridefile) {

		Map<String,Map<String,String>> sections = ((IniFile)overridefile).getSections();
		for (String sectionname: sections.keySet()) {
			if (RulesRASections.hasMatchingType(sectionname)) {
				RulesRASections section = RulesRASections.getMatchingType(sectionname);

				// Based on the enum name, the file describes that type of object
				if (section.name().startsWith(PREFIX_FACTION)) {
					// TODO: Nothing to override with factions yet
				}
				else if (section.name().startsWith(PREFIX_SOUNDTRACK)) {
					// TODO: Nothing to override with soundtracks
				}
				else if (section.name().startsWith(PREFIX_UNIT_INF)) {
					unitsRA.put(sectionname, getUnitByID(sectionname));
					units.put(sectionname, createInfantry(sectionname, sections.get(sectionname)));
				}
			}
		}
	}

	/**
	 * Creates an XML infantry unit from the RA ini-style infantry unit.
	 * 
	 * @param infid	  ID of the infantry unit.
	 * @param infdata RA INI-style infantry data.
	 * @return An <code>XMLInfantry</code> object describing the same unit.
	 */
	private static XMLInfantry createInfantry(String infid, Map<String,String> infdata) {

		XMLInfantry infantry = new XMLInfantry();
		infantry.setID(infid);

		for (String attribname: infdata.keySet()) {
			try {
				RulesRAUnitAttribs unitattrib = RulesRAUnitAttribs.getMatchingType(attribname);
				String methodname = unitattrib.name().substring(5);
				Method setmethod = null;

				// Get the set() method for the attribute
				Method[] methods = infantry.getClass().getMethods();
				for (Method method: methods) {
					if (method.getName().equalsIgnoreCase("set" + methodname)) {
						setmethod = method;
						break;
					}
				}

				// Discover the appropriate type for the attribute value, and set it thus
				Type param = setmethod.getParameterTypes()[0];
				if (param == Integer.class) {
					setmethod.invoke(infantry, Integer.parseInt(infdata.get(attribname)));
				}
			}
			catch (EnumConstantNotPresentException ecnpe) {
				throw new UnknownAttributeException(Strings.getText(
						RulesStringsKeys.UNKNOWN_ATTRIBUTE_UNIT, attribname, infid));
			}
		}
		return infantry;
	}

	/**
	 * @inheritDoc
	 */
	public void revertToDefault() {

	}
}
