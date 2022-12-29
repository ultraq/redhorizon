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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
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

import groovy.transform.TupleConstructor
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

	// For object lifecycles
	private final CopyOnWriteArrayList<SceneElement> addedElements = new CopyOnWriteArrayList<>()
	private final CopyOnWriteArrayList<SceneElement> removedElements = new CopyOnWriteArrayList<>()

	// For object culling
	private final List<GraphicsElement> visibleElements = []
	private final Matrix4f lastCameraView = new Matrix4f()
	private final AtomicBoolean sceneChanged = new AtomicBoolean(true)

	private final List<RenderPass> renderPasses = []
	private final List<OverlayRenderPass> overlayPasses = []

	/**
	 * Constructor, configure the rendering pipeline.
	 * 
	 * @param config
	 * @param context
	 * @param renderer
	 * @param imGuiLayer
	 * @param inputEventStream
	 * @param scene
	 * @param camera
	 */
	RenderPipeline(GraphicsConfiguration config, GraphicsContext context, GraphicsRenderer renderer,
		ImGuiLayer imGuiLayer, InputEventStream inputEventStream, Scene scene, Camera camera) {

		this.renderer = renderer
		this.imGuiLayer = imGuiLayer
		this.scene = scene
		this.camera = camera

		// Build the standard rendering pipeline, including the debug and control
		// overlays as they'll be standard for as long as this thing is in
		// development
		configurePipeline(scene, config, context)

		def debugOverlay = new DebugOverlayRenderPass(renderer, config.debug)
		context.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_D) {
					debugOverlay.enabled = !debugOverlay.enabled
				}
			}
		}
		overlayPasses << debugOverlay

		var controlsOverlay = new ControlsOverlayRenderPass(inputEventStream)
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_C) {
					controlsOverlay.enabled = !controlsOverlay.enabled
				}
			}
		}
		overlayPasses << controlsOverlay

		// Allow for changes to the pipeline from the GUI
		imGuiLayer.on(ChangeEvent) { event ->
			def postProcessingRenderPass = renderPasses.find { renderPass ->
				return renderPass instanceof PostProcessingRenderPass && renderPass.material.shader.name == event.name
			}
			postProcessingRenderPass.enabled = event.value
		}
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
	}

	/**
	 * Build out the rendering pipeline.
	 * 
	 * @param scene
	 * @param config
	 * @param context
	 */
	private void configurePipeline(Scene scene, GraphicsConfiguration config, GraphicsContext context) {

		// Scene render pass
		renderPasses << new SceneRenderPass(scene, renderer.createFramebuffer(context.renderResolution, true))

		var framebufferUniform = new Uniform('framebuffer') {
			@Override
			void apply(Material material, ShaderUniformConfig shaderConfig) {
				shaderConfig.setUniformTexture(name, 0, material.texture.textureId)
			}
		}
		def modelUniform = new Uniform('model') {
			@Override
			void apply(Material material, ShaderUniformConfig shaderConfig) {
				shaderConfig.setUniformMatrix(name, material.transform)
			}
		}
		def textureTargetSizeUniform = new Uniform('textureTargetSize') {
			@Override
			void apply(Material material, ShaderUniformConfig shaderConfig) {
				shaderConfig.setUniform(name, context.targetResolution as float[])
			}
		}

		// Sharp upscaling post-processing pass
		def sharpUpscalingPostProcessingRenderPass = new PostProcessingRenderPass(
			renderer.createFramebuffer(context.targetResolution, false),
			renderer.createMaterial(
				mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
				shader: renderer.createShader(
					'SharpUpscaling',
					'nz/net/ultraq/redhorizon/engine/graphics/opengl',
					framebufferUniform,
					modelUniform,
					new Uniform('textureSourceSize') {
						@Override
						void apply(Material material, ShaderUniformConfig shaderConfig) {
							shaderConfig.setUniform(name, context.renderResolution as float[])
						}
					},
					textureTargetSizeUniform
				)
			),
			true
		)
		renderPasses << sharpUpscalingPostProcessingRenderPass

		// Scanline post-processing pass
		def scanlinePostProcessingRenderPass = new PostProcessingRenderPass(
			renderer.createFramebuffer(context.targetResolution, false),
			renderer.createMaterial(
				mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
				shader: renderer.createShader(
					'Scanlines',
					'nz/net/ultraq/redhorizon/engine/graphics/opengl',
					framebufferUniform,
					modelUniform,
					new Uniform('textureSourceSize') {
						@Override
						void apply(Material material, ShaderUniformConfig shaderConfig) {
							def scale = context.renderResolution.height / context.targetResolution.height / 2 as float
							shaderConfig.setUniform(name, context.renderResolution * scale as float[])
						}
					},
					textureTargetSizeUniform
				)
			),
			config.scanlines
		)
		renderPasses << scanlinePostProcessingRenderPass

		// Final pass to emit the result to the screen
		renderPasses << new ScreenRenderPass(
			renderer.createMaterial(
				mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
				shader: renderer.createShader(
					'Screen',
					'nz/net/ultraq/redhorizon/engine/graphics/opengl',
					framebufferUniform,
					modelUniform
				)
			),
			!config.startWithChrome,
			context
		)

		// Control these passes with the keyboard
		context.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_S) {
					scanlinePostProcessingRenderPass.enabled = !scanlinePostProcessingRenderPass.enabled
				}
				else if (event.key == GLFW_KEY_U) {
					sharpUpscalingPostProcessingRenderPass.enabled = !sharpUpscalingPostProcessingRenderPass.enabled
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
			camera.render(renderer)

			// Perform all rendering passes
			def sceneResult = renderPasses.inject(null) { lastResult, renderPass ->
				if (renderPass.enabled) {
					renderer.setRenderTarget(renderPass.framebuffer)
					renderer.clear()
					renderPass.render(renderer, lastResult)
					return renderPass.framebuffer
				}
				return lastResult
			}
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
	private class SceneRenderPass implements RenderPass<Object> {

		final Scene scene
		final Framebuffer framebuffer
		final boolean enabled = true

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
		void render(GraphicsRenderer renderer, Object nothing) {

			// Initialize or delete objects which have been added/removed to/from the scene
			if (addedElements) {
				def elementsToInit = new ArrayList<SceneElement>(addedElements)
				elementsToInit.each { elementToInit ->
					elementToInit.accept { element ->
						if (element instanceof GraphicsElement) {
							element.init(renderer)
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
				def currentCameraView = camera.view
				if (sceneChanged.get() || !currentCameraView.equals(lastCameraView)) {
					visibleElements.clear()
					def frustumIntersection = new FrustumIntersection(camera.projection * currentCameraView)
					scene.accept { element ->
						if (element instanceof GraphicsElement && frustumIntersection.testPlaneXY(element.bounds)) {
							visibleElements << element
						}
					}
					sceneChanged.compareAndSet(true, false)
					lastCameraView.set(currentCameraView)
				}
			}

			visibleElements.each { element ->
				element.render(renderer)
			}
		}

		@Override
		void setEnabled(boolean enabled) {

			// Does nothing, this pass is always used
		}
	}

	/**
	 * A post-processing render pass for taking a previous framebuffer and
	 * applying some effect to it for the next post-processing pass.
	 */
	@TupleConstructor
	private class PostProcessingRenderPass implements RenderPass<Framebuffer> {

		final Framebuffer framebuffer
		final Material material
		boolean enabled

		@Override
		void delete(GraphicsRenderer renderer) {

			renderer.deleteFramebuffer(framebuffer)
			renderer.deleteMaterial(material)
		}

		@Override
		void render(GraphicsRenderer renderer, Framebuffer previous) {

			material.texture = previous.texture
			renderer.drawMaterial(material)
		}
	}

	/**
	 * The final render pass for taking the result of the post-processing chain
	 * and drawing it to the screen.
	 */
	private class ScreenRenderPass implements RenderPass<Framebuffer> {

		final Framebuffer framebuffer = null
		final Material material
		boolean enabled

		/**
		 * Constructor, create a basic material that covers the screen yet responds
		 * to changes in output/window resolution.
		 * 
		 * @param material
		 * @param enabled
		 * @param context
		 */
		ScreenRenderPass(Material material, boolean enabled, GraphicsContext context) {

			this.material = material
			this.enabled = enabled

			material.transform.set(calculateScreenModelMatrix(context.framebufferSize, context.targetResolution))
			context.on(FramebufferSizeEvent) { event ->
				material.transform.set(calculateScreenModelMatrix(event.framebufferSize, event.targetResolution))
			}

			context.on(KeyEvent) { event ->
				if (event.action == GLFW_PRESS) {
					if (event.key == GLFW_KEY_O) {
						this.enabled = !this.enabled
					}
				}
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
			renderer.drawMaterial(material)
		}
	}
}
