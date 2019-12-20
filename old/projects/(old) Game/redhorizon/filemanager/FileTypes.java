
// ===========================================
// Scanner's Java - List of all the file types
// ===========================================

package redhorizon.filemanager;

import redhorizon.filetypes.UnsupportedFileException;
import redhorizon.strings.Strings;

import static redhorizon.filemanager.MetaTypes.*;

/**
 * Contains a list of all filetypes supported by the Red Horizon commons
 * package.  Although the list can be easily inferred by looking at the
 * packages, this enum also maps file extensions to file implementations and
 * contains lots of other useful type information.
 * 
 * @author Emanuel Rabina
 */
public enum FileTypes {

	// C&C proprietary formats
	AUD_CC ("aud.AudFile", SOUND, "aud"),
	AUD_MUSIC_CC ("aud.AudFileStreaming", SOUNDTRACK, "aud"),
	MIX_CC ("mix.MixFile", ARCHIVE, "mix"),
	PAL_CC ("pal.PalFile", PALETTE, "pal"),
	SHP_CC ("shp.ShpFile", IMAGES | ANIMATION, "shp"),
	TMP_RA ("tmp.TmpFileRA", IMAGES, "int", "sno", "tem"),
	TMP_TD ("tmp.TmpFileTD", IMAGES, "tmp"),
	VQA_CC ("vqa.VqaFile", VIDEO, "vqa"),
	WSA_CC ("wsa.WsaFileCNC", ANIMATION, "wsa"),

	// Generic files
	BMP ("bmp.BmpFile", IMAGE, "bmp"),
	GIF ("BinaryFileBridge", NULL, "gif"),
	INI ("ini.IniFile", MAP | MISSION, "ini"),
	PCX ("pcx.PcxFile", IMAGE, "pcx"),
	PNG ("png.PngFile", IMAGE, "png"),
	XML ("TextFileBridge", MAP | MISSION, "xml");

	public final String implementation;
	public final int type;
	public final String[] extensions;

	/**
	 * Constructor, initializes each enumerated type.
	 * 
	 * @param implementation Corresponding implementing class.
	 * @param type			 File format of this file type/extension.
	 * @param extensions	 List of extensions the implementation supports.
	 */
	private FileTypes(String implementation, int type, String... extensions) {

		this.implementation = "redhorizon.filetypes." + implementation;
		this.type           = type;

		this.extensions = new String[extensions.length];
		for (int i = 0; i < extensions.length; i++) {
			this.extensions[i] = "." + extensions[i];
		}
	}

	/**
	 * Attempts to get the matching type for the filename provided.
	 * 
	 * @param match Name of the file, including file extension.
	 * @return The matching enumerated type.
	 */
	public static FileTypes getMatchingType(String match) {

		// Check the current enum's extension list
		for (FileTypes filetype: FileTypes.values()) {
			for (String fileext: filetype.extensions) {
				if (match.endsWith(fileext)) {
					return filetype;
				}
			}
		}
		throw new UnsupportedFileException(
				Strings.getText(FileManagerStringsKeys.UNSUPPORTED_FILETYPE, match));
	}

	/**
	 * Attempts to get the matching type for the filename and metatype value
	 * provided.
	 * 
	 * @param match Name of the file, including file extension.
	 * @param type	Type of the file.
	 * @return The matching enumerated type.
	 */
	public static FileTypes getMatchingType(String match, int type) {

		for (FileTypes filetype: FileTypes.values()) {

			// Match type first, then check extensions
			if ((filetype.type & type) == type) {
				for (String fileext: filetype.extensions) {
					if (match.contains(fileext)) {
						return filetype;
					}
				}
			}
		}
		throw new UnsupportedFileException(
				Strings.getText(FileManagerStringsKeys.UNSUPPORTED_FILETYPE, match));
	}

	/**
	 * Returns whether the given file extension is one supported by the game.
	 * 
	 * @param ext The file extension to check.
	 * @return <code>true</code> if the file extension is supported,
	 * 		   <code>false</code> otherwise.
	 */
	public static boolean isExtensionSupported(String ext) {

		for (FileTypes fileextension: FileTypes.values()) {
			for (String fileext: fileextension.extensions) {
				if (ext.equals(fileext)) {
					return true;
				}
			}
		}
		return false;
	}
}
