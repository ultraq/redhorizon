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

import nz.net.ultraq.redhorizon.engine.ElementLifecycleState
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ChangeEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiDebugOverlay
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.engine.ElementLifecycleState.*

import org.joml.FrustumIntersection
import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor

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
	final ImGuiDebugOverlay debugOverlay
	final Scene scene
	final Camera camera
	private Dimension targetResolution

	private final List<RenderPass> renderPasses = []

	/**
	 * Constructor, configure the rendering pipeline.
	 * 
	 * @param config
	 * @param context
	 * @param renderer
	 * @param debugOverlay
	 * @param scene
	 * @param camera
	 */
	RenderPipeline(GraphicsConfiguration config, GraphicsContext context, GraphicsRenderer renderer,
		ImGuiDebugOverlay debugOverlay, Scene scene, Camera camera) {

		this.renderer = renderer
		this.debugOverlay = debugOverlay
		this.scene = scene
		this.camera = camera

		targetResolution = context.targetResolution
		context.on(FramebufferSizeEvent) { event ->
			targetResolution = new Dimension(event.width, event.height)
		}

		configurePipeline(config, context)

		// Connect to the debug overlay to configure the pipeline at runtime
		debugOverlay.on(ChangeEvent) { event ->
			switch (event.name) {
				case 'Scanlines': {
					def scanlineShaderRenderPass = renderPasses.find { renderPass ->
						return renderPass instanceof PostProcessingRenderPass &&
							renderPass.material.shader.name == event.name
					}
					scanlineShaderRenderPass.enabled = event.value
					break
				}
			}
		}
	}

	@Override
	void close() {

		renderPasses*.delete(renderer)
	}

	/**
	 * Build out the rendering pipeline.
	 * 
	 * @param config
	 * @param context
	 */
	private void configurePipeline(GraphicsConfiguration config, GraphicsContext context) {

		// Scene render pass
		renderPasses << new SceneRenderPass(renderer.createFramebuffer(context.renderResolution, true))

		def modelUniform = new Uniform('model', { material ->
			return material.transform.get(new float[16])
		})
		def textureTargetSizeUniform = new Uniform<float>('textureTargetSize', { material ->
			return context.targetResolution as float[]
		})

		// Sharp bilinear upscale post-processing pass
		renderPasses << new PostProcessingRenderPass(
			renderer.createFramebuffer(context.targetResolution, false),
			renderer.createMaterial(
				mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
				shader: renderer.createShader('SharpBilinear',
					modelUniform,
					new Uniform<float>('textureSourceSize', { material ->
						return context.renderResolution as float[]
					}),
					textureTargetSizeUniform
				)
			),
			true
		)

		// Scanline post-processing pass
		renderPasses << new PostProcessingRenderPass(
			renderer.createFramebuffer(context.targetResolution, false),
			renderer.createMaterial(
				mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
				shader: renderer.createShader('Scanlines',
					modelUniform,
					new Uniform<float>('textureSourceSize', { material ->
						return context.renderResolution * 0.5 as float[]
					}),
					textureTargetSizeUniform
				)
			),
			config.scanlines
		)

		// Final pass to emit the result to the screen
		renderPasses << new ScreenRenderPass(
			renderer.createMaterial(
				mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
				shader: renderer.createShader('Screen', modelUniform)
			)
		)
	}

	/**
	 * Run through each of the rendering passes configured in the pipeline.
	 */
	void render() {

		// Start a new frame
		debugOverlay.startFrame()
		renderer.clear()
		camera.render(renderer)

		// Reduce the list of renderable items to those just visible in the scene
		def visibleElements = []
		def frustumIntersection = new FrustumIntersection(camera.projection * camera.view)
		averageNanos('objectCulling', 1f, logger) { ->
			scene.accept { element ->
				if (element instanceof GraphicsElement && frustumIntersection.testPlaneXY(element.bounds)) {
					visibleElements << element
				}
			}
		}

		// Perform all rendering passes
		renderPasses.inject(visibleElements) { lastResult, renderPass ->
			if (renderPass.enabled) {
				renderer.setRenderTarget(renderPass.framebuffer)
				renderer.clear()
				renderPass.render(renderer, lastResult)
				return renderPass.framebuffer
			}
			return lastResult
		}

		// Draw overlays
		debugOverlay.render()
		debugOverlay.endFrame()
	}

	/**
	 * The render pass for drawing the scene to a framebuffer at the rendering
	 * resolution.
	 */
	@TupleConstructor(defaults = false, includes = ['framebuffer'])
	class SceneRenderPass implements RenderPass<List<GraphicsElement>> {

		final Framebuffer framebuffer
		final boolean enabled = true
		private final Map<GraphicsElement, ElementLifecycleState> graphicsElementStates = [:]

		@Override
		void delete(GraphicsRenderer renderer) {

			graphicsElementStates.keySet().each { graphicsElement ->
				graphicsElement.delete(renderer)
			}
		}

		@Override
		void render(GraphicsRenderer renderer, List<GraphicsElement> visibleElements) {

			visibleElements.each { element ->
				if (!graphicsElementStates[element]) {
					graphicsElementStates << [(element): STATE_NEW]
				}
				def elementState = graphicsElementStates[element]
				if (elementState == STATE_NEW) {
					element.init(renderer)
					elementState = STATE_INITIALIZED
					graphicsElementStates << [(element): elementState]
				}
				element.render(renderer)
			}
		}
	}

	/**
	 * A post-processing render pass for taking a previous framebuffer and
	 * applying some effect to it for the next post-processing pass.
	 */
	@TupleConstructor
	class PostProcessingRenderPass implements RenderPass<Framebuffer> {

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
	@TupleConstructor(includes = ['material'])
	class ScreenRenderPass implements RenderPass<Framebuffer> {

		final Framebuffer framebuffer = null
		final Material material
		final boolean enabled = true

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
