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
import nz.net.ultraq.redhorizon.engine.graphics.imgui.DebugOverlayRenderPass
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.ElementAddedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.ElementRemovedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneElement

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

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
		this.scene = scene
		this.camera = camera

		fullScreenQuad = renderer.createSpriteMesh(
			surface: new Rectanglef(-1, -1, 1, 1)
		)

		def debugOverlay = new DebugOverlayRenderPass(renderer, config.debug)
		window.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_D) {
					debugOverlay.toggle()
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
		renderer.deleteMesh(fullScreenQuad)
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
		renderPasses << new SceneRenderPass(scene, renderer.createFramebuffer(window.renderResolution, true))

		// Sharp upscaling post-processing pass
		def sharpUpscalingPostProcessingRenderPass = new PostProcessingRenderPass(
			renderer.createFramebuffer(window.targetResolution, false),
			renderer.createMaterial(),
			renderer.createShader(new SharpUpscalingShader()),
			true
		)
		renderPasses << sharpUpscalingPostProcessingRenderPass

		// Scanline post-processing pass
		def scanlinePostProcessingRenderPass = new PostProcessingRenderPass(
			renderer.createFramebuffer(window.targetResolution, false),
			renderer.createMaterial(),
			renderer.createShader(new ScanlinesShader()),
			config.scanlines
		)
		renderPasses << scanlinePostProcessingRenderPass

		// Final pass to emit the result to the screen
		var screenRenderPass = new ScreenRenderPass(
			renderer.createMaterial(),
			renderer.createShader(new ScreenShader()),
			!config.startWithChrome,
			window
		)
		renderPasses << screenRenderPass

		// Control these passes with the keyboard
		window.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_S) {
					scanlinePostProcessingRenderPass.toggle()
				}
				else if (event.key == GLFW_KEY_U) {
					sharpUpscalingPostProcessingRenderPass.toggle()
				}
				else if (event.key == GLFW_KEY_O) {
					screenRenderPass.toggle()
				}
			}
		}
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

	/**
	 * The render pass for drawing the scene to a framebuffer at the rendering
	 * resolution.
	 */
	private class SceneRenderPass implements RenderPass<Boolean> {

		final Scene scene
		final Set<GraphicsElement> initialized = new HashSet<>()
		final Framebuffer framebuffer

		// For object lifecycles
		private final CopyOnWriteArrayList<SceneElement> addedElements = new CopyOnWriteArrayList<>()
		private final CopyOnWriteArrayList<SceneElement> removedElements = new CopyOnWriteArrayList<>()

		// For object culling
		private final List<GraphicsElement> visibleElements = []
		private final AtomicBoolean sceneChanged = new AtomicBoolean(true)

		SceneRenderPass(Scene scene, Framebuffer framebuffer) {

			this.scene = scene
			this.scene.on(ElementAddedEvent) { event ->
				addedElements << event.element
				sceneChanged.set(true)
			}
			this.scene.on(ElementRemovedEvent) { event ->
				removedElements << event.element
				sceneChanged.set(true)
			}

			this.framebuffer = framebuffer
			this.enabled = true
		}

		@Override
		void delete(GraphicsRenderer renderer) {

			scene.accept { element ->
				if (element instanceof GraphicsElement) {
					element.delete(renderer)
				}
			}
		}

		@Override
		void render(GraphicsRenderer renderer, Boolean cameraMoved) {

			// Initialize or delete objects which have been added/removed to/from the scene
			if (addedElements) {
				def elementsToInit = new ArrayList<SceneElement>(addedElements)
				elementsToInit.each { elementToInit ->
					elementToInit.accept { element ->
						if (element instanceof GraphicsElement) {
							element.init(renderer)
							initialized << element
						}
					}
				}
				addedElements.removeAll(elementsToInit)
			}
			if (removedElements) {
				def elementsToDelete = new ArrayList<SceneElement>(removedElements)
				elementsToDelete.each { elementToDelete ->
					elementToDelete.accept { element ->
						if (element instanceof GraphicsElement) {
							element.delete(renderer)
						}
					}
				}
				removedElements.removeAll(elementsToDelete)
			}

			// Reduce the list of renderable items to those just visible in the scene
			averageNanos('objectCulling', 1f, logger) { ->
				if (sceneChanged.get() || cameraMoved) {
					visibleElements.clear()
					def frustumIntersection = new FrustumIntersection(camera.projection * camera.view)
					scene.accept { element ->
						if (element instanceof GraphicsElement && frustumIntersection.testPlaneXY(element.bounds) &&
							initialized.contains(element)) {
							visibleElements << element
						}
					}
					sceneChanged.compareAndSet(true, false)
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

		final Framebuffer framebuffer
		final Material material
		final Shader shader

		PostProcessingRenderPass(Framebuffer framebuffer, Material material, Shader shader, boolean enabled) {

			this.framebuffer = framebuffer
			this.material = material
			this.shader = shader
			this.enabled = enabled
		}

		@Override
		void delete(GraphicsRenderer renderer) {

			renderer.deleteFramebuffer(framebuffer)
			renderer.deleteMaterial(material)
		}

		@Override
		void render(GraphicsRenderer renderer, Framebuffer previous) {

			material.texture = previous.texture
			renderer.draw(fullScreenQuad, shader, material)
		}
	}

	/**
	 * The final render pass for taking the result of the post-processing chain
	 * and drawing it to the screen.
	 */
	private class ScreenRenderPass implements RenderPass<Framebuffer> {

		final Framebuffer framebuffer = null
		final Material material
		final Shader shader

		/**
		 * Constructor, create a basic material that covers the screen yet responds
		 * to changes in output/window resolution.
		 *
		 * @param material
		 * @param shader
		 * @param enabled
		 * @param window
		 */
		ScreenRenderPass(Material material, Shader shader, boolean enabled, Window window) {

			this.material = material
			this.shader = shader
			this.enabled = enabled

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

			renderer.deleteMaterial(material)
		}

		@Override
		void render(GraphicsRenderer renderer, Framebuffer previous) {

			material.texture = previous.texture
			renderer.draw(fullScreenQuad, shader, material)
		}
	}
}
