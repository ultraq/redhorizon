/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.scenegraph

import nz.net.ultraq.eventhorizon.EventTarget

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * A scene is a collection of nodes, with parent/child relationships, that
 * represent the state of all or part of the game world.
 *
 * @author Emanuel Rabina
 */
class Scene implements EventTarget<Scene>, AutoCloseable {

	@Delegate(
		includes = ['addChild', 'clear', 'findAncestor', 'findDescendent', 'insertBefore', 'leftShift', 'removeChild',
			'rotate', 'scale', 'translate', 'traverse'],
		interfaces = false
	)
	final Node root = new RootNode()

	private final Queue<Closure> changeQueue = new ArrayDeque<>()
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()

	@Override
	void close() {

		traverse { node ->
			if (node instanceof AutoCloseable) {
				node.close()
			}
		}
	}

	/**
	 * Apply modifications to the scene that were queued with calls to
	 * {@link #queueChange(Closure)}.
	 */
	void processQueuedChanges() {

		while (changeQueue) {
			changeQueue.poll().call()
		}
	}

	/**
	 * Queue some scene modification to be performed with the next call to
	 * {@link #processQueuedChanges()}.
	 *
	 * TODO: Need to figure out some way of limiting the closure to just queue
	 *       changes - you could put basically anything into here.  One idea might
	 *       be explicit methods for each type of change, eg: a create method for
	 *       adding an entity, that must be given the parent node it's being made
	 *       for.
	 */
	void queueChange(Closure change) {

		changeQueue.add(change)
	}

	/**
	 * Schedule a change to occur after the given amount of time has elapsed.
	 */
	void scheduleChange(long delay, TimeUnit timeUnit, Closure change) {

		executor.schedule({ ->
			queueChange(change)
		}, delay, timeUnit)
	}

	/**
	 * A special instance of {@link Node} that is always present in the scene.
	 */
	private class RootNode extends Node<RootNode> {

		@Override
		Scene getScene() {

			return Scene.this
		}
	}
}
