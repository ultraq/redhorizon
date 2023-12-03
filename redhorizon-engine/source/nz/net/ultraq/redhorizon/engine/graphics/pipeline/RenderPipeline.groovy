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

import nz.net.ultraq.redhorizon.engine.graphics.Camera
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ChangeEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ControlsOverlayRenderPass
import nz.net.ultraq.redhorizon.engine.graphics.imgui.DebugOverlayRenderPass
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

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
	final Scene scene
	final Camera camera

	private final Mesh fullScreenMesh
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
		this.scene = scene
		this.camera = camera

		fullScreenMesh = renderer.createSpriteMesh(
			surface: new Rectanglef(-1, -1, 1, 1)
		)

		def debugOverlay = new DebugOverlayRenderPass(renderer, config.debug)
		window.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_D) {
					debugOverlay.toggle()
					logger.debug("Debug output ${debugOverlay.enabled ? 'enabled' : 'disabled'}")
				}
			}
		}
		overlayPasses << debugOverlay

		var controlsOverlay = new ControlsOverlayRenderPass(inputEventStream)
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_C) {
					controlsOverlay.toggle()
				}
			}
		}
		overlayPasses << controlsOverlay

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
		configurePipeline(scene, config, window)
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
		renderer.deleteMesh(fullScreenMesh)
	}

	/**
	 * Build out the rendering pipeline.
	 *
	 * @param scene
	 * @param config
	 * @param window
	 */
	private void configurePipeline(Scene scene, GraphicsConfiguration config, Window window) {

		// Scene render pass
		renderPasses << new SceneRenderPass(scene, camera, renderer.createFramebuffer(window.renderResolution, true))

		// Sharp upscaling post-processing pass
		def sharpUpscalingPostProcessingRenderPass = new SharpUpscalingPostProcessingRenderPass(fullScreenMesh, renderer, window)
		window.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS && event.key == GLFW_KEY_U) {
				sharpUpscalingPostProcessingRenderPass.toggle()
				logger.debug("Sharp upscaling ${sharpUpscalingPostProcessingRenderPass.enabled ? 'enabled' : 'disabled'}")
			}
		}
		renderPasses << sharpUpscalingPostProcessingRenderPass

		// Scanline post-processing pass
		def scanlinePostProcessingRenderPass = new ScanlinePostProcessingRenderPass(fullScreenMesh, renderer, window, config)
		window.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS && event.key == GLFW_KEY_S) {
				scanlinePostProcessingRenderPass.toggle()
				logger.debug("Scanlines ${scanlinePostProcessingRenderPass.enabled ? 'enabled' : 'disabled'}")
			}
		}
		renderPasses << scanlinePostProcessingRenderPass

		// Final pass to emit the result to the screen
		var screenRenderPass = new ScreenRenderPass(fullScreenMesh, renderer, window, config)
		window.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS && event.key == GLFW_KEY_O) {
				screenRenderPass.toggle()
			}
		}
		renderPasses << screenRenderPass
	}

	/**
	 * Run through each of the rendering passes configured in the pipeline.
	 */
	void render() {

		// Start a new frame
		imGuiLayer.frame { ->
			renderer.clear()
			var cameraMoved = camera.update()

			// Perform all rendering passes
			def sceneResult = renderPasses.inject(cameraMoved) { lastResult, renderPass ->
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
}
