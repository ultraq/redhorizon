/*
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli.objectviewer

import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.input.KeyEvent

import static org.lwjgl.glfw.GLFW.*

/**
 * Common viewer application code.
 *
 * @author Emanuel Rabina
 */
abstract class Viewer extends Application {

	/**
	 * Constructor, set viewer-specific configuration.
	 *
	 * @param audioConfig
	 * @param graphicsConfig
	 */
	protected Viewer(AudioConfiguration audioConfig, GraphicsConfiguration graphicsConfig) {

		super(null, audioConfig, graphicsConfig)
	}

//	@Override
	protected void applicationStart() {

		var scaleIndex = scaleRange.findIndexOf { it == initialScale }
		graphicsSystem.camera.scale(scaleRange[scaleIndex])

		// Key event handler
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
					case GLFW_KEY_ESCAPE:
						stop()
						break
				}
			}
		}
	}

	/**
	 * Return the initial scale of the camera to apply to this viewer.
	 *
	 * @return
	 */
	protected abstract float getInitialScale()

	/**
	 * Return the range of scaling options available for this viewer.
	 *
	 * @return
	 */
	protected abstract float[] getScaleRange()
}
