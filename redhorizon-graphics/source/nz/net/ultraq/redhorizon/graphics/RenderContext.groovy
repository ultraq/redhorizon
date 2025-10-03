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

import nz.net.ultraq.redhorizon.graphics.opengl.RenderTarget

/**
 * A set of rendering commands that can be used by objects while this context is
 * valid.  These include shortcuts to applying uniforms to a shader without
 * requiring knowledge of how the shader is constructed.
 *
 * @author Emanuel Rabina
 */
interface RenderContext {

	/**
	 * Set the render target for subsequent draw calls.
	 */
	void setRenderTarget(RenderTarget renderTarget)
}
