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
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiDebugOverlay
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.engine.ElementLifecycleState.*

import org.joml.FrustumIntersection
import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
	private final Map<GraphicsElement, ElementLifecycleState> graphicsElementStates = [:]

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

		// Scene render pass
		def sceneFramebuffer = renderer.createFramebuffer(context.renderResolution, true)
		renderPasses << new RenderPass(
			framebuffer: sceneFramebuffer,
			operation: { Material material, List<GraphicsElement> visibleElements ->
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
		)

		def modelUniform = new Uniform('model', { material ->
			return material.transform.get(new float[16])
		})
		def textureTargetSizeUniform = new Uniform<float>('textureTargetSize', { material ->
			return context.targetResolution as float[]
		})

		// Sharp bilinear upscale post-processing pass
		renderPasses << new RenderPass(
			framebuffer: renderer.createFramebuffer(context.targetResolution, false),
			material: renderer.createMaterial(
				mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
				shader: renderer.createShader('SharpBilinear',
					modelUniform,
					new Uniform<float>('textureSourceSize', { material ->
						return context.renderResolution as float[]
					}),
					textureTargetSizeUniform
				)
			),
			operation: { Material material, Framebuffer framebuffer ->
				material.texture = framebuffer.texture
				renderer.drawMaterial(material)
			}
		)

		// Scanline post-processing pass
		if (config.scanlines) {
			renderPasses << new RenderPass(
				framebuffer: renderer.createFramebuffer(context.targetResolution, false),
				material: renderer.createMaterial(
					mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
					shader: renderer.createShader('Scanlines',
						modelUniform,
						new Uniform<float>('textureSourceSize', { material ->
							return context.renderResolution * 0.5 as float[]
						}),
						textureTargetSizeUniform
					)
				),
				operation: { Material material, Framebuffer framebuffer ->
					material.texture = framebuffer.texture
					renderer.drawMaterial(material)
				}
			)
		}

		// Final pass to emit the result to the screen
		renderPasses << new RenderPass(
			material: renderer.createMaterial(
				mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
				shader: renderer.createShader('Screen', modelUniform)
			),
			operation: { Material material, Framebuffer framebuffer ->
				material.texture = framebuffer.texture
				renderer.drawMaterial(material)
			}
		)
	}

	@Override
	void close() {

		graphicsElementStates.keySet().each { graphicsElement ->
			graphicsElement.delete(renderer)
		}
		renderPasses.each { renderPass ->
			if (renderPass.framebuffer) {
				renderer.deleteFramebuffer(renderPass.framebuffer)
			}
			if (renderPass.material) {
				renderer.deleteMaterial(renderPass.material)
			}
		}
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
			renderer.setRenderTarget(renderPass.framebuffer)
			renderer.clear()
			renderPass.operation(renderPass.material, lastResult)
			return renderPass.framebuffer
		}

		// Draw overlays
		debugOverlay.drawDebugOverlay()
		debugOverlay.endFrame()
	}
}
