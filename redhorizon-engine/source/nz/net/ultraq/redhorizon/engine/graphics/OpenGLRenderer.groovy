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

package nz.net.ultraq.redhorizon.engine.graphics

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLCapabilities
import static org.lwjgl.opengl.GL11C.*

/**
 * Common code between both of the OpenGL renderers.
 * 
 * @author Emanuel Rabina
 */
abstract class OpenGLRenderer implements GraphicsRenderer {

	protected final GLCapabilities capabilities
	protected final Colour clearColour
	protected final boolean filter

	/**
	 * Constructor, copies out the configuration.
	 * 
	 * @param config
	 */
	OpenGLRenderer(GraphicsConfiguration config) {

		capabilities = GL.createCapabilities()

		clearColour = config.clearColour
		filter = config.filter

		glClearColor(clearColour.r, clearColour.g, clearColour.b, 1)
	}

	@Override
	void clear() {

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
	}
}
