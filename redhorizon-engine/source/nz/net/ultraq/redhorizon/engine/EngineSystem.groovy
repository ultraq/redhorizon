/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.PackageScope
import java.util.concurrent.CyclicBarrier

/**
 * A system provides behaviour for a component or set of components.  Systems
 * traverse a {@link Scene}, looking for the components they work with, and then
 * do something with the data in those components.
 * <p>
 * Systems operate on their own thread, and should be careful not to step on
 * data that could be used by other systems/threads.
 *
 * @author Emanuel Rabina
 */
abstract class EngineSystem implements Runnable, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(EngineSystem)

	private final CyclicBarrier processStartBarrier = new CyclicBarrier(2)
	private final CyclicBarrier processCompleteBarrier = new CyclicBarrier(2)
	private Engine engine

	protected Scene scene

	/**
	 * Configure the current scene with this system.
	 */
	abstract void configureScene()

	/**
	 * Return the engine that this system is a part of.
	 */
	protected Engine getEngine() {

		return engine
	}

	/**
	 * Return the name of the system, for logging purposes.
	 */
	protected String getSystemName() {

		return this.class.simpleName
	}

	/**
	 * Return what part of the game loop this system will be used for.
	 */
	protected abstract EngineSystemType getType()

	/**
	 * Called by the system at the end of a processing iteration to let the engine
	 * know it has finished.
	 *
	 * @see {@link #process}
	 */
	protected void notifyForProcessComplete() {

		processCompleteBarrier.await()
		processCompleteBarrier.reset()
	}

	/**
	 * Called by the engine to release the system waiting on {@link #waitForProcessStart}
	 * so that it can begin a processing iteration.
	 */
	protected void notifyForProcessStart() {

		processStartBarrier.await()
		processStartBarrier.reset()
	}

	/**
	 * A convenience method to execute the given closure between a {@link #waitForProcessStart}
	 * call and a {@link #notifyForProcessComplete} call.
	 */
	protected void process(Closure closure) {

		waitForProcessStart()
		closure()
		notifyForProcessComplete()
	}

	/**
	 * Controlled initialization, execution loop, and shutdown or an engine
	 * system.
	 */
	@Override
	final void run() {

		logger.debug('{}: starting', systemName)
		runInit()
		trigger(new EngineSystemReadyEvent())

		logger.debug('{}: in loop', systemName)
		runLoop()

		logger.debug('{}: shutting down', systemName)
		trigger(new EngineSystemStoppedEvent())
		runShutdown()
		logger.debug('{}: stopped', systemName)
	}

	/**
	 * Initialize any resources for the running of the system here.
	 */
	protected void runInit() {
	}

	/**
	 * Enter the loop for the system to continually work on the scene.
	 */
	protected abstract void runLoop()

	/**
	 * Cleanup/free any resources created by the system here.
	 */
	protected void runShutdown() {
	}

	/**
	 * Set the engine this system is a part of.
	 */
	@PackageScope
	void setEngine(Engine engine) {

		this.engine = engine
	}

	/**
	 * Called by the engine to wait for the signal by the system that processing
	 * has completed.
	 */
	protected void waitForProcessComplete() {

		processCompleteBarrier.await()
	}

	/**
	 * Called by the system to wait for the signal from the engine that processing
	 * may continue.  A system should call this at the start of a processing loop.
	 *
	 * @see {@link #process}
	 */
	protected void waitForProcessStart() {

		processStartBarrier.await()
	}
}
