/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader

import groovy.transform.TupleConstructor

/**
 * A post-processing render pass for taking a previous framebuffer and
 * applying some effect to it for the next post-processing pass.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false, post = { args ->
	this.enabled = args.enabled
})
abstract class PostProcessingRenderPass implements RenderPass<Framebuffer> {

	final Mesh fullScreenMesh
	final Framebuffer framebuffer
	final Material material
	final Shader shader

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteFramebuffer(framebuffer)
		renderer.deleteMaterial(material)
	}

	@Override
	void render(GraphicsRenderer renderer, Framebuffer previous) {

		material.texture = previous.texture
		renderer.draw(fullScreenMesh, shader, material)
	}
}
