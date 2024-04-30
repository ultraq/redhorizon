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

package nz.net.ultraq.redhorizon.engine

import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.audio.AudioSystem
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.ImGuiElement
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.time.TimeSystem
import nz.net.ultraq.redhorizon.events.EventTarget
import static nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent.EVENT_TYPE_STOP

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor
import java.util.concurrent.Semaphore

/**
 * A base for developing an application that uses the Red Horizon engine and
 * various systems.  This class uses a builder-like API to let you pick what
 * goes into the application, as well as customize it at certain event
 * lifecycles, eg:
 * <p>
 * <pre>{@code
 * new Application("Window title")
 *   .addGraphicsSystem(myGraphicsConfig)
 *   .useScene(scene)
 *   .onApplicationStart(application -> {
 *     // Setup here
 *   })
 *   .start()
 *}</pre>
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class Application implements EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(Application)

	final String windowTitle

	private final Engine engine = new Engine()
	private final InputEventStream inputEventStream = new InputEventStream()

	private Scene scene
	private ApplicationEventHandler applicationStart
	private ApplicationEventHandler applicationStop
	private boolean applicationStopping
	private Semaphore applicationStoppingSemaphore = new Semaphore(1)
	private DebugOverlay debugOverlay

	/**
	 * Add the audio system to this application.  The audio system will run on its
	 * own thread.
	 */
	Application addAudioSystem(AudioConfiguration config = new AudioConfiguration()) {

		engine << new AudioSystem(config)
		return this
	}

	/**
	 * Add the graphics system to this application.  The graphics system will run
	 * on its own thread, and the window it creates will be the source of user
	 * input into the application.
	 */
	Application addGraphicsSystem(GraphicsConfiguration config = new GraphicsConfiguration(),
		ImGuiElement... overlayRenderPasses) {

		var graphicsSystem = new GraphicsSystem(windowTitle, inputEventStream, config)
		graphicsSystem.on(WindowCreatedEvent) { event ->
			inputEventStream.addInputSource(event.window)
		}
		graphicsSystem.on(SystemReadyEvent) { event ->
			var audioSystem = engine.systems.find { it instanceof AudioSystem } as AudioSystem
			debugOverlay = new DebugOverlay(config.debug)
				.addAudioRenderer(audioSystem.renderer)
				.addGraphicsRenderer(graphicsSystem.renderer)
				.toggleWith(inputEventStream, GLFW_KEY_D)
			graphicsSystem.renderPipeline.addImGuiElement(debugOverlay)
			overlayRenderPasses.each { overlayRenderPass ->
				graphicsSystem.renderPipeline.addImGuiElement(overlayRenderPass)
			}
		}
		graphicsSystem.relay(WindowMaximizedEvent, this)
		engine << graphicsSystem
		return this
	}

	/**
	 * Add a managed time system to this application.  The time system will update
	 * objects in the scene on its own thread.
	 */
	Application addTimeSystem() {

		engine << new TimeSystem()
		return this
	}

	/**
	 * Use this application with the given scene.  If this method is never used,
	 * then an empty scene will be created during startup.
	 */
	Application useScene(Scene scene) {

		this.scene = scene
		return this
	}

	/**
	 * Start the application.
	 */
	final void start() {

		logger.debug('Initializing application...')

		if (!scene) {
			scene = new Scene()
		}

		// Universal quit on exit
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS && event.key == GLFW_KEY_ESCAPE) {
				stop()
			}
		}
		inputEventStream.on(GuiEvent) { event ->
			if (event.type == EVENT_TYPE_STOP) {
				stop()
			}
		}

		// Start the application
		logger.debug('Starting application...')
		engine.start()
		engine.systems*.scene = scene
		scene.inputEventStream = inputEventStream
		applicationStart?.apply(this, scene)

		engine.waitUntilStopped()

		// Check we closed everything
		var activeFramebuffers = debugOverlay.activeFramebuffers.get()
		if (activeFramebuffers > 0) {
			logger.debug('Not all active framebuffers closed')
		}
		var activeMeshes = debugOverlay.activeMeshes.get()
		if (activeMeshes > 0) {
			logger.debug('Not all meshes closed, {} remaining', activeMeshes)
		}
		var activeTextures = debugOverlay.activeTextures.get()
		if (activeMeshes > 0) {
			logger.debug('Not all textures closed, {} remaining', activeTextures)
		}
		var activeSources = debugOverlay.activeSources.get()
		if (activeMeshes > 0) {
			logger.debug('Not all sources closed, {} remaining', activeSources)
		}
		var activeBuffers = debugOverlay.activeBuffers.get()
		if (activeMeshes > 0) {
			logger.debug('Not all buffers closed, {} remaining', activeBuffers)
		}
	}

	/**
	 * Stop the application.
	 */
	final void stop() {

		applicationStoppingSemaphore.tryAcquireAndRelease { ->
			if (!applicationStopping) {
				logger.debug('Stopping application...')
				applicationStop?.apply(this, scene)
				engine.stop()
				applicationStopping = true
			}
		}
	}

	/**
	 * Configure some code to run after engine startup.
	 */
	Application onApplicationStart(ApplicationEventHandler handler) {

		applicationStart = handler
		return this
	}

	/**
	 * Configure some code to run before engine shutdown.
	 */
	Application onApplicationStop(ApplicationEventHandler handler) {

		applicationStop = handler
		return this
	}

	/**
	 * The functional interface called for each application lifecycle event.  It
	 * is provided the application itself, plus either the configured or default
	 * scene, whichever is current at that point in time.
	 */
	@FunctionalInterface
	static interface ApplicationEventHandler {

		void apply(Application application, Scene scene);
	}
}
