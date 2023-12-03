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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.ShaderConfig
import nz.net.ultraq.redhorizon.engine.graphics.Window

import org.joml.Matrix4f

import groovy.transform.PackageScope

/**
 * The final render pass for taking the result of the post-processing chain and
 * drawing it to the screen.
 *
 * @author Emanuel Rabina
 */
@PackageScope
class ScreenRenderPass implements RenderPass<Framebuffer> {

	final Mesh fullScreenMesh
	final Framebuffer framebuffer = null
	final Material material
	final Shader shader

	/**
	 * Constructor, create a basic material that covers the screen yet responds
	 * to changes in output/window resolution.
	 */
	ScreenRenderPass(Mesh fullScreenMesh, GraphicsRenderer renderer, Window window, GraphicsConfiguration config) {

		this.fullScreenMesh = fullScreenMesh
		this.material = renderer.createMaterial()
		this.shader = renderer.createShader(new ScreenShader())
		this.enabled = !config.startWithChrome

		material.transform.set(calculateScreenModelMatrix(window.framebufferSize, window.targetResolution))
		window.on(FramebufferSizeEvent) { event ->
			material.transform.set(calculateScreenModelMatrix(event.framebufferSize, event.targetResolution))
		}
	}

	/**
	 * Return a matrix for adjusting the final texture drawn to screen to
	 * accomodate the various screen/window sizes while respecting the target
	 * resolution's aspect ratio.
	 *
	 * @param framebufferSize
	 * @param targetResolution
	 * @return
	 */
	private static Matrix4f calculateScreenModelMatrix(Dimension framebufferSize, Dimension targetResolution) {

		return new Matrix4f()
			.scale(
				framebufferSize.aspectRatio > targetResolution.aspectRatio ?
					1 - ((framebufferSize.width - targetResolution.width) / framebufferSize.width) as float : // Window is wider
					1,
				framebufferSize.aspectRatio < targetResolution.aspectRatio ?
					1 - ((framebufferSize.height - targetResolution.height) / framebufferSize.height) as float : // Window is taller
					1,
				1
			)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void render(GraphicsRenderer renderer, Framebuffer previous) {

		material.texture = previous.texture
		renderer.draw(fullScreenMesh, shader, material)
	}

	/**
	 * Configuration for the Screen shader.
	 */
	private class ScreenShader extends ShaderConfig {

		/**
		 * Constructor, create the screen shader.
		 */
		ScreenShader() {

			super(
				'Screen',
				'nz/net/ultraq/redhorizon/engine/graphics/pipeline/Screen.vert.glsl',
				'nz/net/ultraq/redhorizon/engine/graphics/pipeline/Screen.frag.glsl',
				Uniforms.framebufferUniform,
				Uniforms.modelUniform
			)
		}
	}
}
