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
import nz.net.ultraq.redhorizon.engine.graphics.Camera
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ChangeEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ControlsOverlayRenderPass
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

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
	final Camera camera

	private final Mesh fullScreenQuad
	private final List<RenderPass> renderPasses = []
	private final List<OverlayRenderPass> overlayPasses = []

	/**
	 * Constructor, configure the rendering pipeline.
	 *
	 * @param config
	 * @param window
	 * @param renderer
	 * @param imGuiLayer
	 * @param inputEventStream
	 * @param scene
	 * @param camera
	 */
	RenderPipeline(GraphicsConfiguration config, Window window, GraphicsRenderer renderer,
		ImGuiLayer imGuiLayer, InputEventStream inputEventStream, Scene scene, Camera camera) {

		this.renderer = renderer
		this.imGuiLayer = imGuiLayer
		this.camera = camera

		fullScreenQuad = renderer.createSpriteMesh(
			surface: new Rectanglef(-1, -1, 1, 1)
		)

		overlayPasses << new ControlsOverlayRenderPass(inputEventStream).toggleWith(inputEventStream, GLFW_KEY_C)

		// Allow for changes to the pipeline from the GUI
		imGuiLayer.on(ChangeEvent) { event ->
			def postProcessingRenderPass = renderPasses.find { renderPass ->
				return renderPass instanceof PostProcessingRenderPass && renderPass.shader.name == event.name
			}
			postProcessingRenderPass.enabled = event.value
		}

		// Build the standard rendering pipeline, including the debug and control
		// overlays as they'll be standard for as long as this thing is in
		// development
		configurePipeline(scene, config, window, inputEventStream)
	}

	/**
	 * Register an overlay rendering pass with the rendering pipeline.  Overlays
	 * are drawn after the scene and use the target resolution of the window.
	 *
	 * @param overlayPass
	 */
	void addOverlayPass(OverlayRenderPass overlayPass) {

		overlayPasses << overlayPass
	}

	@Override
	void close() {

		renderPasses*.delete(renderer)
		renderer.deleteMesh(fullScreenQuad)
	}

	/**
	 * Build out the rendering pipeline.
	 */
	private void configurePipeline(Scene scene, GraphicsConfiguration config, Window window, InputEventStream inputEventStream) {

		// Scene render pass
		renderPasses << new SceneRenderPass(scene, renderer.createFramebuffer(window.renderResolution, true))

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
		renderPasses << new PostProcessingRenderPass(
			renderer.createFramebuffer(window.targetResolution, false),
			renderer.createShader(new ScanlinesShader()),
			config.scanlines
		)
			.toggleWith(inputEventStream, GLFW_KEY_S) { renderPass ->
				logger.debug("Scanlines ${renderPass.enabled ? 'enabled' : 'disabled'}")
			}

		// Final pass to emit the result to the screen
		renderPasses << new ScreenRenderPass(
			renderer.createShader(new ScreenShader()),
			!config.startWithChrome,
			window
		)
			.toggleWith(inputEventStream, GLFW_KEY_O)
	}

	/**
	 * Run through each of the rendering passes configured in the pipeline.
	 */
	void render() {

		// Start a new frame
		imGuiLayer.frame { ->
			renderer.clear()
			camera.update()

			// Perform all rendering passes
			def sceneResult = renderPasses.inject(null) { lastResult, renderPass ->
				if (renderPass.enabled) {
					renderer.setRenderTarget(renderPass.framebuffer)
					renderer.clear()
					renderPass.render(renderer, lastResult)
					return renderPass.framebuffer
				}
				return lastResult
			} as Framebuffer
			renderer.setRenderTarget(null)

			// Draw overlays
			imGuiLayer.render(sceneResult)
			overlayPasses.each { overlayPass ->
				if (overlayPass.enabled) {
					overlayPass.render(renderer, sceneResult)
				}
			}
		}
	}

	/**
	 * The render pass for drawing the scene to a framebuffer at the rendering
	 * resolution.
	 */
	private class SceneRenderPass implements RenderPass<Void> {

		final Scene scene
		final Framebuffer framebuffer

		// For object culling
		private final List<GraphicsElement> visibleElements = []

		SceneRenderPass(Scene scene, Framebuffer framebuffer) {

			this.scene = scene
			this.framebuffer = framebuffer
			this.enabled = true
		}

		@Override
		void render(GraphicsRenderer renderer, Void unused) {

			// Cull the list of renderable items to those just visible in the scene
			averageNanos('objectCulling', 1f, logger) { ->
				visibleElements.clear()
				var frustumIntersection = new FrustumIntersection(camera.projection * camera.view)
				scene.accept { Node element ->
					if (element instanceof GraphicsElement && frustumIntersection.testPlaneXY(element.globalBounds)) {
						visibleElements << element
					}
				}
			}

			visibleElements.each { element ->
				element.render(renderer)
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
		void delete(GraphicsRenderer renderer) {

			renderer.deleteFramebuffer(framebuffer)
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
		 *
		 * @param shader
		 * @param enabled
		 * @param window
		 */
		ScreenRenderPass(Shader shader, boolean enabled, Window window) {

			this.shader = shader
			this.enabled = enabled

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
		}

		@Override
		void render(GraphicsRenderer renderer, Framebuffer previous) {

			material.texture = previous.texture
			renderer.draw(fullScreenQuad, transform, shader, material)
		}
	}
}
