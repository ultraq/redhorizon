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

package redhorizon.launcher.tasks;

import java.nio.file.Paths;

/**
 * Load all of the standard C&C Red Alert files as a mod.
 *
 * @author Emanuel Rabina
 */
public class LoadModRedAlertTask extends LoadModTask {

	private static final String RED_ALERT_MOD_DIRECTORY = "_Red Alert";

	/**
	 * Constructor, create the load Red Alert task.
	 */
	public LoadModRedAlertTask() {

		super(Paths.get(MOD_DIRECTORY, RED_ALERT_MOD_DIRECTORY));
	}
}
