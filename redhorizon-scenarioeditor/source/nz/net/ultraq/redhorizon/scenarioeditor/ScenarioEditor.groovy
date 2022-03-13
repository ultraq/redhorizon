/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.scenarioeditor

import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Application for viewing and editing classic Command & Conquer maps/missions.
 * 
 * @author Emanuel Rabina
 */
class ScenarioEditor extends Application {

	private static final Logger logger = LoggerFactory.getLogger(ScenarioEditor)
	private static final Dimension renderResolution = new Dimension(1280, 800)

	private final Palette palette

	/**
	 * Constructor, sets up an application with the default configurations.
	 *
	 * @param version
	 * @param palette
	 */
	ScenarioEditor(String version, Palette palette) {

		super("Red Horizon Scenario Editor ${version}",
			new AudioConfiguration(),
			new GraphicsConfiguration(
				renderResolution: renderResolution,
				startWithChrome: true
			)
		)

		this.palette = palette
	}

	@Override
	void run() {

	}
}
