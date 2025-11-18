/*
 * Copyright 2016, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * A coordinator object responsible for starting/stopping any registered
 * {@link EngineSystem}s that operate on a {@link Scene}.  Each system is given
 * it's own thread to take advantage of multi-processor CPUs.
 *
 * @author Emanuel Rabina
 */
class Engine implements EventTarget<Engine> {

	private static final Logger logger = LoggerFactory.getLogger(Engine)

	private final List<EngineSystem> allSystems = []
	private final List<EngineSystem> inputSystems = []
	private final List<EngineSystem> updateSystems = []
	private final List<EngineSystem> renderSystems = []
	private final Semaphore enginesStoppingSemaphore = new Semaphore(1)

	private Thread engineThread
	private List<Thread> systemThreads
	private boolean engineStopping
	private Scene scene

	/**
	 * Add a system to the engine.
	 */
	void addSystem(EngineSystem system) {

		switch (system.type) {
			case EngineSystemType.INPUT -> inputSystems << system
			case EngineSystemType.UPDATE -> updateSystems << system
			case EngineSystemType.RENDER -> renderSystems << system
		}
		allSystems << system
		system.engine = this
	}

	/**
	 * Find and return the given system, if present.
	 */
	<T extends EngineSystem> T findSystem(Class<T> clazz) {

		return (T)allSystems.find { system -> system.class == clazz }
	}

	/**
	 * Groovy overload of {@code <<} to call {@link #addSystem}.
	 */
	void leftShift(EngineSystem system) {

		addSystem(system)
	}

	/**
	 * Start the game engine and the main game loop.  This will assign all systems
	 * their own thread on which to start and run.  This method will block until
	 * all systems have completed execution.
	 *
	 * @throws Throwable
	 *   Any exception that may have occurred during the running of the engine.
	 */
	void start() {

		engineThread = Thread.currentThread()

		// Engine setup
		// -------------------------------------------------------------------------

		logger.debug('Starting engine...')

		Throwable uncaughtException
		var threadBuilder = Thread.ofPlatform()
			.uncaughtExceptionHandler { Thread t, Throwable e ->
				logger.error('An error occurred in thread {}', t.name)
				uncaughtException = e
				stop()
			}

		var engineReadyLatch = new CountDownLatch(allSystems.size())
		systemThreads = allSystems.collect() { system ->
			system.on(EngineSystemReadyEvent) { event ->
				engineReadyLatch.countDown()
			}
			system.on(EngineSystemStoppedEvent) { event ->
				stop()
			}
			return threadBuilder
				.name(system.class.simpleName)
				.start(system)
		}

		while (true) {
			engineReadyLatch.await(1, TimeUnit.SECONDS)
			if (engineReadyLatch.count == 0) {
				break
			}
			if (uncaughtException) {
				throw uncaughtException
			}
		}
		logger.debug('Engine started')
		trigger(new EngineReadyEvent())

		// Game loop
		// -------------------------------------------------------------------------

		logger.debug('Beginning game loop')
		while (!Thread.interrupted() && !engineStopping) {
			try {
				inputSystems*.notifyForProcessStart()
				inputSystems*.waitForProcessComplete()

				updateSystems*.notifyForProcessStart()
				updateSystems*.waitForProcessComplete()

				renderSystems*.notifyForProcessStart()
				renderSystems*.waitForProcessComplete()
			}
			catch (InterruptedException ignored) {
				break
			}
		}

		logger.debug('Waiting for each system to signal completion')
		systemThreads*.join()

		// Engine shutdown
		// -------------------------------------------------------------------------

		if (uncaughtException) {
			throw uncaughtException
		}
		logger.debug('Engine stopped')
	}

	/**
	 * Set the scene the engine is running the game loop over.
	 */
	void setScene(Scene scene) {

		this.scene = scene
		allSystems*.scene = scene
		allSystems*.configureScene()
	}

	/**
	 * Signal to stop the game engine.
	 */
	void stop() {

		enginesStoppingSemaphore.tryAcquireAndRelease { ->
			if (!engineStopping) {
				engineThread.interrupt()
				systemThreads*.interrupt()
				engineStopping = true
			}
		}
	}
}
