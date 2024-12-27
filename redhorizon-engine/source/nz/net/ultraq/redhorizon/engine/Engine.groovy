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

import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * A coordinator object responsible for starting/stopping the engine systems
 * that operate on a scene.  Each system can be given it's own thread to take
 * advantage of multi-processor CPUs.
 *
 * @author Emanuel Rabina
 */
class Engine implements EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(Engine)

	final List<EngineSystem> systems = []

	private final Semaphore enginesStoppingSemaphore = new Semaphore(1)

	private List<Thread> systemThreads
	private boolean engineStopping

	/**
	 * Add a system to the engine.
	 */
	void addSystem(EngineSystem system) {

		systems << system
		system.engine = this
	}

	/**
	 * Groovy overload of {@code <<} to call {@link #addSystem}.
	 */
	void leftShift(EngineSystem system) {

		addSystem(system)
	}

	/**
	 * Start the game engine.  This will assign all systems their own thread to
	 * run.  This method will block until all systems have completed execution.
	 *
	 * @throws Throwable
	 *   Any exception that may have occurred during the running of the engine.
	 */
	void start() {

		logger.debug('Starting engine...')

		Throwable uncaughtException
		var threadBuilder = Thread.ofPlatform()
			.uncaughtExceptionHandler { Thread t, Throwable e ->
				logger.error('An error occurred in thread {}', t.name)
				uncaughtException = e
			}

		var engineReadyLatch = new CountDownLatch(systems.size())
		systemThreads = systems.collect() { system ->
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

		logger.debug('Waiting for each system to signal completion')
		systemThreads*.join()
		if (uncaughtException) {
			throw uncaughtException
		}
		logger.debug('Engine stopped')
	}

	/**
	 * Signal to stop the game engine.
	 */
	void stop() {

		enginesStoppingSemaphore.tryAcquireAndRelease { ->
			if (!engineStopping) {
				systemThreads*.interrupt()
				engineStopping = true
			}
		}
	}
}
