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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Future.State
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * The engine is responsible for running the systems that operate on a scene.
 * Each system can be given it's own thread to take advantage of multi-processor
 * CPUs.
 *
 * @author Emanuel Rabina
 */
class Engine {

	private static final Logger logger = LoggerFactory.getLogger(Engine)

	final List<EngineSystem> systems = []

	private final ExecutorService executorService = Executors.newCachedThreadPool()
	private final Semaphore enginesStoppingSemaphore = new Semaphore(1)

	private List<Future<?>> systemTasks
	private boolean engineStopped

	/**
	 * Add a system to the engine.
	 *
	 * @param system
	 */
	void addSystem(EngineSystem system) {

		systems << system
	}

	/**
	 * Groovy overload of {@code <<} to call {@link #addSystem}.
	 *
	 * @param system
	 */
	void leftShift(EngineSystem system) {

		addSystem(system)
	}

	/**
	 * Start the game engine.  This will assign all systems their own thread to
	 * run.  This method will block until all systems have signalled their ready
	 * status.
	 */
	void start() {

		logger.debug('Starting engine...')

		var engineReadyLatch = new CountDownLatch(systems.size())
		systemTasks = systems.collect() { system ->
			system.on(SystemReadyEvent) { event ->
				engineReadyLatch.countDown()
			}
			system.on(SystemStoppedEvent) { event ->
				stop()
			}
			return executorService.submit(system)
		}

		while (true) {
			engineReadyLatch.await(1, TimeUnit.SECONDS)
			if (engineReadyLatch.count == 0) {
				break
			}
			var failedTasks = systemTasks.findAll { task -> task.state() == State.FAILED }
			if (failedTasks) {
				failedTasks.each { failedTask ->
					logger.error('An error occurred during engine startup', failedTask)
				}
				throw failedTasks.first().exceptionNow()
			}
		}

		logger.debug('Engine started')
	}

	/**
	 * Signal to stop the game engine.
	 */
	void stop() {

		enginesStoppingSemaphore.tryAcquireAndRelease { ->
			if (!engineStopped) {
				systemTasks*.cancel(true)
				engineStopped = true
				logger.debug('Engine stopped')
			}
		}
	}

	/**
	 * Wait here until the engine has stopped/completed.
	 */
	void waitUntilStopped() {

		systemTasks.each { systemTask ->
			try {
				systemTask.get()
			}
			catch (CancellationException ignored) {
				// Do nothing
			}
		}
		logger.debug('All systems stopped')
	}
}
