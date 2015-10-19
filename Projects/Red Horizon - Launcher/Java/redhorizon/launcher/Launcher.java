/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.launcher;

import redhorizon.engine.Engine;
import redhorizon.engine.display.DisplayWindow;
import redhorizon.engine.display.SplashScreen;
import redhorizon.engine.display.SplashScreenTask;
import redhorizon.engine.display.Window;
import redhorizon.engine.display.swt.SWTDisplayWindow;
import redhorizon.engine.display.swt.SWTSplashScreen;
import redhorizon.filetypes.ini.IniFile;
import redhorizon.launcher.swt.LauncherSWT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.TreeMap;

/**
 * Launcher main class.
 * 
 * @author Emanuel Rabina
 */
public abstract class Launcher extends Window {

	protected TreeMap<IniFile,File> modfiles;

	/**
	 * Run the launcher.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Create a new launcher
		final Launcher launcher = new LauncherSWT();

		// Create and open the splash screen
		SplashScreen splashscreen = new SWTSplashScreen("Red Horizon",
				Engine.RED_HORIZON_VERSION, "Launcher/SplashScreen_Default.png");
		splashscreen.addSplashScreenTask(new SplashScreenTask() {
			@Override
			public void doTask() {
				launcher.scanForMods(false);
			}
			@Override
			public String taskText() {
				return "Scanning for installed mods...";
			}
		});
		splashscreen.open();

		// Open the launcher
		launcher.open();

		// Open a new window
		DisplayWindow window = new SWTDisplayWindow("Red Horizon test", new TestRenderer());
		window.open();
	}

	/**
	 * Subclass-only constructor.
	 */
	protected Launcher() {
	}

	/**
	 * Scan for <tt>mod.ini</tt> files inside the Mods directory.
	 * 
	 * @param refresh Set to <tt>true</tt> to force a rescan of the Mods
	 * 				  directory.
	 */
	protected void scanForMods(boolean refresh) {

		if (modfiles == null || refresh) {
			modfiles = new TreeMap<IniFile,File>();
			modfiles.putAll(scanForMods(new File("Mods")));
		}
	}

	/**
	 * Recursively scans a directory for <tt>mod.ini</tt> files.
	 * 
	 * @param root Root directory to start the scanning from.
	 * @return Map of <tt>mod.ini</tt> files and the folders in which they're
	 * 		   found.
	 */
	protected TreeMap<IniFile,File> scanForMods(File root) {

		final TreeMap<IniFile,File> moddirs = new TreeMap<IniFile,File>();

		// Add mod.ini files in current directory, tracking subdirectories along the way
		File[] dirs = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.equals("mod.ini")) {
					IniFile modini = new IniFile("mod.ini", new BufferedReader(
							new FileReader(new File(dir, name))));
					moddirs.put(modini, dir);
				}
				return dir.isDirectory();
			}
		});

		// Scan subdirectories
		for (File dir: dirs) {
			moddirs.putAll(scanForMods(dir));
		}

		return moddirs;
	}
}
