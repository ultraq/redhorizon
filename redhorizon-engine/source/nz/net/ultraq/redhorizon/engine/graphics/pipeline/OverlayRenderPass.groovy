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
import nz.net.ultraq.redhorizon.engine.input.InputEventStream

/**
 * A rendering pass for drawing overlay content to the game viewport.
 *
 * @author Emanuel Rabina
 */
interface OverlayRenderPass extends RenderPass<Framebuffer> {

	/**
	 * Returns {@code null} for overlay render passes.
	 *
	 * @return {@code null}
	 */
	@Override
	default Framebuffer getFramebuffer() {

		return null
	}

	/**
	 * Render the overlay.
	 *
	 * @param renderer
	 * @param sceneFramebufferResult
	 */
	void render(GraphicsRenderer renderer, Framebuffer sceneFramebufferResult)

	@Override
	default OverlayRenderPass toggleWith(InputEventStream inputEventStream, int key) {

		return super.toggleWith(inputEventStream, key) as OverlayRenderPass
	}
}
