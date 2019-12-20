
// =====================================
// Scanner's Java - File manager strings
// =====================================

package redhorizon.filemanager;

import redhorizon.strings.StringsKey;

public enum FileManagerStringsKeys implements StringsKey {

	// File management errors
	NO_FILEEXTENSION,
	MISSING_ARCHIVE,
	MISSING_FILE,
	UNSUPPORTED_ARCHIVE,
	UNSUPPORTED_FILETYPE;

	private static final String RESOURCEBUNDLE_FILEMANAGER = "Game_FileManager";

	/**
	 * @inheritDoc
	 */
	public String getKey() {

		return name();
	}

	/**
	 * @inheritDoc
	 */
	public String getResourceBundle() {

		return RESOURCEBUNDLE_FILEMANAGER;
	}
}
