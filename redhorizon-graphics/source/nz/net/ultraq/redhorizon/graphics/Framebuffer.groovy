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

package nz.net.ultraq.redhorizon.graphics

/**
 * A section of memory that can be drawn to like a screen, and can then be used
 * as input for further rendering steps.
 *
 * @author Emanuel Rabina
 */
interface Framebuffer extends GraphicsResource {

	/**
	 * Draw the contents of the framebuffer to the current render target.
	 */
	void draw(PostProcessingShaderContext shaderContext)

	/**
	 * Get the height of the framebuffer.
	 */
	int getHeight()

	/**
	 * Get the width of the framebuffer.
	 */
	int getWidth()

	/**
	 * Use this framebuffer as the render target for the next set of render
	 * operations.  This will last until either another render target is selected
	 * for rendering.
	 */
	void useFramebuffer(Closure closure)
}
