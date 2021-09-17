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

import groovy.transform.MapConstructor

/**
 * A single rendering pass performed by the renderer, includes the target
 * framebuffer to render to and if any special shader is used for the pass.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor(post = {
	if (material) {
		material.texture = framebuffer.texture
		material.shader = shader
	}
})
class RenderPass {

	final Framebuffer framebuffer
	final Shader shader
	final Material material
}
