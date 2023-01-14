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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Semaphore

/**
 * The engine is responsible for running the systems that operate on a scene.
 * Each system can be given it's own thread to take advantage of multi-processor
 * CPUs.
 *
 * @author Emanuel Rabina
 */
class Engine {

	private static final Logger logger = LoggerFactory.getLogger(Engine)

	private final ExecutorService executorService = Executors.newCachedThreadPool()
	private final List<EngineSystem> systems = []
	private List<Future<?>> systemTasks

	private final Semaphore enginesStoppingSemaphore = new Semaphore(1)
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
	 * Start the game engine.  This will assign all entity systems their own
	 * thread to operate on the scene.  This method will block until all systems
	 * have signalled their ready status.
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
		engineReadyLatch.await()

		logger.debug('Engine started')
	}

	/**
	 * Signal to stop the game engine.
	 */
	void stop() {

		enginesStoppingSemaphore.tryAcquireAndRelease { ->
			if (!engineStopped) {
				systems*.stop()
				engineStopped = true
				logger.debug('Engine stopped')
			}
		}
	}

	/**
	 * Wait here until the engine has stopped/completed.
	 */
	void waitUntilStopped() {

		systemTasks*.get()
		logger.debug('All systems stopped')
	}
}
