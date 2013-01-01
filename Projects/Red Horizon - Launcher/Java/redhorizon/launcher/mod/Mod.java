/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package redhorizon.launcher.mod;

import redhorizon.filetypes.ini.IniFile;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Map;

/**
 * Contains information about a mod, gleamed from the mod.ini file found in a
 * mod directory.
 * 
 * @author Emanuel Rabina
 */
public class Mod {

	private static final String MOD_INI_FILE        = "Mod.ini";
	private static final String MOD_SECTION         = "Mod";
	private static final String MOD_KEY_NAME        = "Name";
	private static final String MOD_KEY_VERSION     = "Version";
	private static final String MOD_KEY_DESCRIPTION = "Description";
	private static final String MOD_KEY_AUTHOR      = "Author";
	private static final String MOD_KEY_WEBSITE     = "Website";
	private static final String MOD_KEY_YEAR        = "Year";
	private static final String MOD_KEY_IMAGE       = "Image";
	private static final String MOD_KEY_REQUIRES    = "Requires";

	private final String name;
	private final String version;
	private final String description;
	private final String author;
	private final String website;
	private final int year;
	private final String imagepath;
	private final String[] dependencies;

	/**
	 * Constructor, attempt to read all mod information from a given path, first
	 * using the Mod.ini file (if any), then by scanning the path for other mod
	 * files.
	 * 
	 * @param path Path to the directory or file containing the mod.
	 */
	public Mod(Path path) {

		// Find Mod.ini file
		Path modinipath = path.resolve(MOD_INI_FILE);
		try (IniFile modinifile = new IniFile(MOD_INI_FILE, FileChannel.open(modinipath))) {

			Map<String,String> moddetails = modinifile.getSection(MOD_SECTION);
			name         = moddetails.get(MOD_KEY_NAME);
			version      = moddetails.get(MOD_KEY_VERSION);
			description  = moddetails.get(MOD_KEY_DESCRIPTION);
			author       = moddetails.get(MOD_KEY_AUTHOR);
			website      = moddetails.get(MOD_KEY_WEBSITE);
			year         = Integer.parseInt(moddetails.get(MOD_KEY_YEAR));
			imagepath    = moddetails.get(MOD_KEY_IMAGE);

			String requires = moddetails.get(MOD_KEY_REQUIRES);
			dependencies = requires != null && !requires.isEmpty() ? requires.split(",") : new String[0];
			for (int i = 0; i < dependencies.length; i++) {
				dependencies[i] = dependencies[i].trim();
			}
		}
	}

	/**
	 * Returns the name of the person or organization that created this mod.
	 * 
	 * @return Author name.
	 */
	public String getAuthor() {

		return author;
	}

	/**
	 * Returns an array of mod names that this mod is itself dependent on.
	 * 
	 * @return Mod dependencies.
	 */
	public String[] getDependencies() {

		return dependencies;
	}

	/**
	 * Returns a description of this mod.
	 * 
	 * @return Mod description.
	 */
	public String getDescription() {

		return description;
	}

	/**
	 * Returns the name of the image to use for this mod in previews.
	 * 
	 * @return Mod image/icon.
	 */
	public String getImagePath() {

		return imagepath;
	}

	/**
	 * Returns the name of this mod.
	 * 
	 * @return Mod name.
	 */
	public String getName() {

		return name;
	}

	/**
	 * Returns the version of this mod.
	 * 
	 * @return Mod version.
	 */
	public String getVersion() {

		return version;
	}

	/**
	 * Returns a URL to the website or home page for this mod.
	 * 
	 * @return Mod website URL.
	 */
	public String getWebsite() {

		return website;
	}

	/**
	 * Returns the year this mod was released.
	 * 
	 * @return Mod release year.
	 */
	public int getYear() {

		return year;
	}
}
