/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * Each of the discrete steps used in creating a single frame and emitting it to
 * the screen.
 *
 * @author Emanuel Rabina
 */
interface RenderPipeline {

	/**
	 * Complete rendering of the frame, flipping buffers and polling for input
	 * events.
	 */
	void end()

	/**
	 * Given the framebuffer from the {@link #scene} step, perform any
	 * post-processing on that framebuffer into another framebuffer of your
	 * choice, which should then be returned in the closure.  The returned
	 * framebuffer will be used as input into the next stages.
	 */
	RenderPipeline postProcessing(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.graphics.Framebuffer') Closure<Framebuffer> closure)

	/**
	 * Perform rendering of the scene to a framebuffer of your choice, which
	 * should then be returned in the closure.  The returned framebuffer will be
	 * used as input into the next stages.
	 */
	RenderPipeline scene(Closure<Framebuffer> closure)

	/**
	 * Given the framebuffer from the {@link #postProcessing} step, emit that
	 * framebuffer to the screen and then draw any UI over the top of it using
	 * ImGui.
	 */
	RenderPipeline ui(boolean createDockspace,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext') Closure closure)
}
