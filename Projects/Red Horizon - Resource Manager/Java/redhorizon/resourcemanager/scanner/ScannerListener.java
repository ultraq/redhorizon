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

package redhorizon.resourcemanager.scanner;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Interface for listener classes to be notified of directory scanning matches.
 * 
 * @author Emanuel Rabina
 * @see DirectoryScanner
 */
public interface ScannerListener {

	/**
	 * Called by a file system scanner when the file or directory it encounters
	 * matches the pattern specified by this listener.
	 * 
	 * @param path File/directory that matched the listener pattern.
	 * @see ScannerListener#pattern()
	 */
	public void match(Path path);

	/**
	 * Regular expression pattern that the file or directory name should match
	 * so that the listener gets notified.
	 * 
	 * @return File or directory name pattern.
	 */
	public Pattern pattern();
}
