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

package nz.net.ultraq.redhorizon.engine.graphics.pipeline

import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Switch
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent

import static org.lwjgl.glfw.GLFW.GLFW_PRESS

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * A single rendering pass with a specific framebuffer as the rendering target.
 *
 * @param <T>
 *   The expected type of input data from a prior rendering pass.
 * @author Emanuel Rabina
 */
interface RenderPass<T> extends Switch {

	/**
	 * Perform any cleanup for this render pass.
	 *
	 * @param renderer
	 */
	void delete(GraphicsRenderer renderer)

	/**
	 * Return the target framebuffer for this rendering pass.
	 *
	 * @return
	 */
	Framebuffer getFramebuffer()

	/**
	 * Perform the render pass, using the expected result of any previous render
	 * pass.
	 *
	 * @param renderer
	 * @param previous
	 */
	void render(GraphicsRenderer renderer, T previous)

	/**
	 * Toggle the state of this render pass with the given key.
	 */
	default RenderPass<T> toggleWith(InputEventStream inputEventStream, int key,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.pipeline.RenderPass') Closure closure = null) {

		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS && event.key == key) {
				toggle()
				if (closure) {
					closure(this)
				}
			}
		}
		return this
	}
}
