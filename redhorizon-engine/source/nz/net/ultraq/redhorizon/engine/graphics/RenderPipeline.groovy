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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL11C.glViewport

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
	final Dimension renderResolution
	private Dimension targetResolution

	private final List<RenderPass> renderPasses = []
	private final List<Material> materialPasses = []
	private final Map<GraphicsElement, ElementLifecycleState> graphicsElementStates = [:]

	/**
	 * Constructor, configure the rendering pipeline.
	 * 
	 * @param context
	 * @param renderer
	 * @param debugOverlay
	 * @param scene
	 * @param camera
	 */
	RenderPipeline(GraphicsContext context, GraphicsRenderer renderer, ImGuiDebugOverlay debugOverlay, Scene scene, Camera camera) {

		this.renderer = renderer
		this.debugOverlay = debugOverlay
		this.scene = scene
		this.camera = camera

		renderResolution = context.renderResolution
		targetResolution = context.targetResolution
		context.on(FramebufferSizeEvent) { event ->
			targetResolution = new Dimension(event.width, event.height)
		}
	}

	/**
	 * Add a rendering pass to this pipeline.
	 * 
	 * @param renderPass
	 */
	void addRenderPass(RenderPass renderPass) {

		// For post-processing passes, create the material to pass to the
		// post-processing pass that will contain the texture of the previous
		// framebuffer
		materialPasses << renderer.createMaterial(
			mesh: renderPass.mesh,
			texture: renderPass.framebuffer.texture,
			shader: renderPass.effect,
			transform: renderPass.transform
		)
		renderPasses << renderPass
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
		}
		materialPasses.each { materialPass ->
			renderer.deleteMaterial(materialPass)
		}
	}

	/**
	 * An alias for {@link #addRenderPass(RenderPass)}
	 * 
	 * @param renderPass
	 */
	void leftShift(RenderPass renderPass) {

		addRenderPass(renderPass)
	}

	/**
	 * Run through each of the rendering passes configured in the pipeline.
	 */
	void render() {

		// Start a new frame
		debugOverlay.startFrame()
		renderer.clear()
		glViewport(0, 0, renderResolution.width, renderResolution.height)
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

		// Scene render pass
		def nextRenderPass = renderPasses[0]
		renderer.setRenderTarget(nextRenderPass?.framebuffer)
		renderer.clear()
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

		// Post-processing rendering passes
		glViewport(0, 0, targetResolution.width, targetResolution.height)
		def previousData = materialPasses[0]
		for (def i = 0; i < renderPasses.size(); i++) {
			def renderPass = renderPasses[i]
			def nextPass = renderPasses[i + 1]
			renderer.setRenderTarget(nextPass?.framebuffer)
			renderer.clear()
			renderPass.operation(previousData)
			previousData = materialPasses[i]
		}

		// Draw overlays
		debugOverlay.drawDebugOverlay()
		debugOverlay.endFrame()
	}
}
