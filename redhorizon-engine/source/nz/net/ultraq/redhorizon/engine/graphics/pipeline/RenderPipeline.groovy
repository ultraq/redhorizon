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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ChangeEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U

/**
 * A render pipeline contains all of the configured rendering passes and
 * executes them so that objects are drawn to a screen.  These passes can be
 * added/removed dynamically, but usually consist of: culling the scene to
 * exclude off-screen objects, rendering the scene, post-processing effects, and
 * overlays.
 *
 * @author Emanuel Rabina
 */
class RenderPipeline implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(RenderPipeline)

	final GraphicsRenderer renderer
	final ImGuiLayer imGuiLayer

	private final Mesh fullScreenQuad
	private final List<RenderPass> renderPasses = []
	private SceneRenderPass sceneRenderPass
	private ScreenRenderPass screenRenderPass

	/**
	 * Constructor, configure the rendering pipeline.
	 */
	RenderPipeline(GraphicsConfiguration config, Window window, GraphicsRenderer renderer, ImGuiLayer imGuiLayer,
		InputEventStream inputEventStream) {

		this.renderer = renderer
		this.imGuiLayer = imGuiLayer

		fullScreenQuad = renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1))

		// Allow for changes to the pipeline from the GUI
		imGuiLayer.on(ChangeEvent) { event ->
			var postProcessingRenderPass = renderPasses.find { renderPass ->
				return renderPass instanceof PostProcessingRenderPass && renderPass.shader.name == event.name
			}
			postProcessingRenderPass.enabled = event.value
		}

		// Build the standard rendering pipeline, including the debug and control
		// overlays as they'll be standard for as long as this thing is in
		// development
		configurePipeline(config, window, inputEventStream)
	}

	@Override
	void close() {

		renderPasses*.close()
		renderer.delete(fullScreenQuad)
	}

	/**
	 * Build out the rendering pipeline.
	 */
	private void configurePipeline(GraphicsConfiguration config, Window window, InputEventStream inputEventStream) {

		// Scene render pass
		sceneRenderPass = new SceneRenderPass(renderer.createFramebuffer(window.renderResolution, true))
		renderPasses << sceneRenderPass

		// Sharp upscaling post-processing pass
		renderPasses << new PostProcessingRenderPass(
			renderer.createFramebuffer(window.targetResolution, false),
			renderer.createShader(new SharpUpscalingShader()),
			true
		)
			.toggleWith(inputEventStream, GLFW_KEY_U) { renderPass ->
				logger.debug("Sharp upscaling ${renderPass.enabled ? 'enabled' : 'disabled'}")
			}

		// Scanline post-processing pass
//		renderPasses << new PostProcessingRenderPass(
//			renderer.createFramebuffer(window.targetResolution, false),
//			renderer.createShader(new ScanlinesShader()),
//			config.scanlines
//		)
//			.toggleWith(inputEventStream, GLFW_KEY_S) { renderPass ->
//				logger.debug("Scanlines ${renderPass.enabled ? 'enabled' : 'disabled'}")
//			}

		// Final pass to emit the result to the screen
		screenRenderPass = new ScreenRenderPass(
			renderer.createShader(new ScreenShader()),
			window
		)
		renderPasses << screenRenderPass
	}

	/**
	 * Collect the objects to draw with the next call to {@link #render}.
	 */
	void gather() {

		sceneRenderPass.gather()
	}

	/**
	 * Run through each of the rendering passes configured in the pipeline.
	 */
	void render() {

		// Start a new frame
		imGuiLayer.frame { ->
			renderer.clear()

			screenRenderPass.enabled = !imGuiLayer.enabled

			// Perform all rendering passes
			var sceneResult = renderPasses.inject((Framebuffer)null) { lastResult, renderPass ->
				if (renderPass.enabled) {
					renderer.setRenderTarget(renderPass.framebuffer)
					renderer.clear()
					renderPass.render(renderer, lastResult)
					return renderPass.framebuffer
				}
				return lastResult
			}
			renderer.setRenderTarget(null)
			return sceneResult
		}
	}

	/**
	 * Apply a scene to this render pipeline.
	 */
	void setScene(Scene scene) {

		sceneRenderPass.scene = scene
	}

	/**
	 * The render pass for drawing the scene to a framebuffer at the rendering
	 * resolution.
	 */
	private class SceneRenderPass implements RenderPass<Void> {

		final Framebuffer framebuffer
		private final List<Node> visibleElements = []
		private final Matrix4f enlargedViewProjection = new Matrix4f()
		private final FrustumIntersection frustumIntersection = new FrustumIntersection()

		Scene scene

		SceneRenderPass(Framebuffer framebuffer) {

			this.framebuffer = framebuffer
			this.enabled = true
		}

		@Override
		void close() {

			renderer.delete(framebuffer)
		}

		/**
		 * Gather a list of renderables to be drawn to the screen with the next call
		 * to {@link #render}.
		 */
		void gather() {

			if (scene) {
				var camera = scene.camera
				if (camera) {
					average('Culling', 1f, logger) { ->
						visibleElements.clear()
						frustumIntersection.set(enlargedViewProjection.scaling(0.9f, 0.9f, 1f).mul(camera.viewProjection), false)
						scene.query(frustumIntersection).each { element ->
							if (element instanceof GraphicsElement) {
								visibleElements << element
							}
						}
					}
				}
			}
		}

		@Override
		void render(GraphicsRenderer renderer, Void unused) {

			if (scene) {
				var camera = scene.camera
				if (camera) {
					camera.render(renderer)

					average('Rendering', 1f, logger) { ->
						visibleElements.each { element ->
							((GraphicsElement)element).render(renderer)
						}
					}
				}
			}
		}
	}

	/**
	 * A post-processing render pass for taking a previous framebuffer and
	 * applying some effect to it for the next post-processing pass.
	 */
	private class PostProcessingRenderPass implements RenderPass<Framebuffer> {

		final Matrix4f transform = new Matrix4f()
		final Material material = new Material()
		final Framebuffer framebuffer
		final Shader shader

		PostProcessingRenderPass(Framebuffer framebuffer, Shader shader, boolean enabled) {

			this.framebuffer = framebuffer
			this.shader = shader
			this.enabled = enabled
		}

		@Override
		void close() {

			renderer.delete(framebuffer)
		}

		@Override
		void render(GraphicsRenderer renderer, Framebuffer previous) {

			material.texture = previous.texture
			renderer.draw(fullScreenQuad, transform, shader, material)
		}
	}

	/**
	 * The final render pass for taking the result of the post-processing chain
	 * and drawing it to the screen.
	 */
	private class ScreenRenderPass implements RenderPass<Framebuffer> {

		final Framebuffer framebuffer = null
		final Matrix4f transform = new Matrix4f()
		final Material material = new Material()
		final Shader shader

		/**
		 * Constructor, create a basic material that covers the screen yet responds
		 * to changes in output/window resolution.
		 */
		ScreenRenderPass(Shader shader, Window window) {

			this.shader = shader

			transform.set(calculateScreenModelMatrix(window.framebufferSize, window.targetResolution))
			window.on(FramebufferSizeEvent) { event ->
				transform.set(calculateScreenModelMatrix(event.framebufferSize, event.targetResolution))
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
		private Matrix4f calculateScreenModelMatrix(Dimension framebufferSize, Dimension targetResolution) {

			return new Matrix4f()
				.scale(
					framebufferSize.aspectRatio > targetResolution.aspectRatio ?
						1 - ((framebufferSize.width() - targetResolution.width()) / framebufferSize.width()) as float : // Window is wider
						1,
					framebufferSize.aspectRatio < targetResolution.aspectRatio ?
						1 - ((framebufferSize.height() - targetResolution.height()) / framebufferSize.height()) as float : // Window is taller
						1,
					1
				)
		}

		@Override
		void close() {
		}

		@Override
		void render(GraphicsRenderer renderer, Framebuffer previous) {

			material.texture = previous.texture
			renderer.draw(fullScreenQuad, transform, shader, material)
		}
	}
}
