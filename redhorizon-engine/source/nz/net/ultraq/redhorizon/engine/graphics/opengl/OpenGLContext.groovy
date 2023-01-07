/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsContext

import org.lwjgl.glfw.GLFWErrorCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*
import static org.lwjgl.system.MemoryUtil.NULL

/**
 * The OpenGL context, a concept used by OpenGL to control rendering threads.
 * Using GLFW, this object represents both the window and the OpenGL context.
 *
 * @author Emanuel Rabina
 */
class OpenGLContext extends GraphicsContext {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLContext)

	@SuppressWarnings('GrFinalVariableAccess')
	final OpenGLWindow window

	/**
	 * Constructor, create a new OpenGL window and context using GLFW.
	 *
	 * @param windowTitle
	 * @param config
	 */
	OpenGLContext(String windowTitle, GraphicsConfiguration config) {

		glfwSetErrorCallback(new GLFWErrorCallback() {
			@Override
			void invoke(int error, long description) {
				def message = getDescription(description)
				logger.error(message)
			}
		})

		if (!glfwInit()) {
			throw new IllegalStateException('Unable to initialize GLFW')
		}

		window = new OpenGLWindow(windowTitle, config)

		withCurrent { ->
			glfwSwapInterval(config.vsync ? 1 : 0)
		}
	}

	/**
	 * Destroys the OpenGL context, which includes the window through which it is
	 * rendering.
	 */
	@Override
	void close() {

		window.close()
		glfwTerminate()
	}

	@Override
	void makeCurrent() {

		glfwMakeContextCurrent(window.window)
	}

	@Override
	void releaseCurrent() {

		glfwMakeContextCurrent(NULL)
	}
}
