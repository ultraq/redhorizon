/* 
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.ContextErrorEvent
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiDebugOverlay
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLContext
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer
import nz.net.ultraq.redhorizon.engine.input.InputEvent
import nz.net.ultraq.redhorizon.engine.input.InputSource
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.engine.ElementLifecycleState.*

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.FutureTask

/**
 * Graphics subsystem, creates a display which drives the rendering loop of
 * drawing graphics objects.
 * 
 * @author Emanuel Rabina
 */
class GraphicsEngine extends Engine implements InputSource {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsEngine)

	private final GraphicsConfiguration config
	private final Scene scene
	private final Closure needsMainThreadCallback

	private OpenGLContext openGlContext
	private Camera camera
	private boolean started

	/**
	 * Constructor, build a new engine for rendering graphics.
	 * 
	 * @param config
	 * @param scene
	 * @param needsMainThreadCallback
	 *   Closure for notifying the caller that a given method (passed as the first
	 *   parameter of the closure) needs invoking.  Some GLFW operations can only
	 *   be done on the main thread, so this indicates to the caller (which is
	 *   often the main thread) to initiate the method call.
	 */
	GraphicsEngine(GraphicsConfiguration config, Scene scene,
		@ClosureParams(value = SimpleType, options = 'java.util.concurrent.FutureTask') Closure needsMainThreadCallback) {

		this.config = config ?: new GraphicsConfiguration()
		this.scene = scene
		this.needsMainThreadCallback = needsMainThreadCallback
	}

	/**
	 * Return the current camera.
	 * 
	 * @return
	 */
	Camera getCamera() {

		return camera
	}

	/**
	 * Return whether or not the graphics engine has been started.
	 * 
	 * @return
	 */
	boolean isStarted() {

		return started
	}

	/**
	 * Return whether or not the graphics engine has been stopped.
	 * 
	 * @return
	 */
	boolean isStopped() {

		return !running || openGlContext.windowShouldClose()
	}

	/**
	 * Start the graphics engine loop: creates a new window in which to render the
	 * elements in the current scene, cleaning it all up when made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Graphics Engine'
		logger.debug('Starting graphics engine')

		// Initialization
		openGlContext = waitForMainThread { ->
			return new OpenGLContext(config)
		}
		openGlContext.withCloseable { context ->
			context.relay(InputEvent, this)
			context.relay(ContextErrorEvent, this)
			context.withCurrent { ->
				camera = new Camera(context.windowSize)
				triggerOnSeparateThread(new WindowCreatedEvent(context.windowSize, camera.size))

				new OpenGLRenderer(context, config).withCloseable { renderer ->
					new ImGuiDebugOverlay(context.window, renderer).withCloseable { debugOverlay ->
						logger.debug(renderer.toString())
						camera.init(renderer)

						def graphicsElementStates = [:]
						List<RenderPass> renderPasses = []

						def modelUniform = new Uniform('model', { material ->
							return material.transform.get(new float[16])
						})
						def textureSourceSizeUniform = new Uniform<float>('textureSourceSize', { material ->
							return new float[] { material.texture.width, material.texture.height }
						})
						def textureTargetSizeUniform = new Uniform<float>('textureTargetSize', { material ->
							return context.framebufferSize as float[]
						})

						// Set up the rendering pipeline for any post-processing steps
						// TODO: Represent this all with a "rendering pipeline" object
						if (config.scanlines) {
							renderPasses << new RenderPass(
								framebuffer: renderer.createFramebuffer(false),
								shader: renderer.createShader('Scanlines', modelUniform, textureSourceSizeUniform, textureTargetSizeUniform),
								material: renderer.createMaterial(
									mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1))
								)
							)
						}

						renderPasses << new RenderPass(
							framebuffer: renderer.createFramebuffer(true),
							shader: renderer.createShader('SharpBilinear', modelUniform, textureSourceSizeUniform, textureTargetSizeUniform),
							material: renderer.createMaterial(
								mesh: renderer.createSpriteMesh(new Rectanglef(-1, -1, 1, 1)),
								transform: new Matrix4f().scale(1, (config.fixAspectRatio ? 1.2 : 1) as float, 1)
							)
						)

						renderPasses << new RenderPass(
							framebuffer: null
						)

						// Rendering loop
						logger.debug('Graphics engine in render loop...')
						started = true
						engineLoop { ->
							debugOverlay.startFrame()

							def scenePass = renderPasses.head()
							renderer.setRenderTarget(scenePass.framebuffer)
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

							// Post-processing
							renderPasses.tail().inject(scenePass) { lastPass, nextPass ->
								renderer.setRenderTarget(nextPass.framebuffer)
								renderer.clear()
								renderer.drawMaterial(lastPass.material)
								return nextPass
							}

							// GUI/Overlays
							debugOverlay.drawDebugOverlay()
							debugOverlay.endFrame()

							context.swapBuffers()
							waitForMainThread { ->
								context.pollEvents()
							}
						}

						// Shutdown
						logger.debug('Shutting down graphics engine')
						renderPasses.each { renderPass ->
							def framebuffer = renderPass.framebuffer
							if (framebuffer) {
								renderer.deleteFramebuffer(framebuffer)
							}
							def material = renderPass.material
							if (material) {
								renderer.deleteMaterial(material)
							}
						}
						camera.delete(renderer)
						graphicsElementStates.keySet().each { graphicsElement ->
							graphicsElement.delete(renderer)
						}
					}
				}
			}
		}
	}

	@Override
	protected boolean shouldRun() {

		return !openGlContext.windowShouldClose() && super.shouldRun()
	}

	@Override
	void stop() {

		if (running) {
			openGlContext.windowShouldClose(true)
		}
		super.stop()
	}

	/**
	 * Put the graphics engine in a wait state until the given task has been
	 * executed by the main thread, returning the result of execution in that
	 * thread.
	 * 
	 * @param closure
	 * @return
	 */
	private <T> T waitForMainThread(Closure<T> closure) {

		def future = new FutureTask<T>(closure)
		needsMainThreadCallback(future)
		return future.get()
	}
}
