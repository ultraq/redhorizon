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

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
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
		includes = ['clear', 'findDescendent', 'findDescendentByType', 'insertBefore', 'leftShift', 'removeChild', 'rotate',
			'scale', 'translate', 'traverse'],
		interfaces = false
	)
	final Node root = new RootNode()

	private final Queue<Closure> updateQueue = new ArrayDeque<>()
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()

	/**
	 * Add a node as a child of the root of the scene.
	 */
	Scene addChild(Node node) {

		root.addChild(node)
		return this
	}

	/**
	 * Add and return a node as a child of the root of the scene.
	 */
	<T extends Node> T addAndReturnChild(T node) {

		return root.addAndReturnChild(node)
	}

	@Override
	void close() {

		traverse { node ->
			if (node instanceof AutoCloseable) {
				node.close()
			}
			return true
		}
		executor.close()
	}

	/**
	 * Return the first node in the scene to match the given predicate.
	 */
	<T extends Node> T find(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.scenegraph.Node')
			Closure<Boolean> predicate) {

		return (T)root.findDescendent(predicate)
	}

	/**
	 * Return the first node in the scene with the given name.
	 */
	<T extends Node> T findByName(String name) {

		return find { node -> node.name == name }
	}

	/**
	 * Return the first node in the scene of the given type.
	 */
	<T extends Node> T findByType(Class<T> type) {

		return find { node -> type.isInstance(node) }
	}

	/**
	 * Queue some scene modification to be performed with the next call to
	 * {@link #update()}.
	 *
	 * TODO: Need to figure out some way of limiting the closure to just queue
	 *       changes - you could put basically anything into here.  One idea might
	 *       be explicit methods for each type of change, eg: a create method for
	 *       adding an entity, that must be given the parent node it's being made
	 *       for.
	 */
	void queueUpdate(Closure change) {

		updateQueue.add(change)
	}

	/**
	 * Queue some scene modification to be performed after the given amount of
	 * time has elapsed.
	 */
	void queueUpdate(long delay, TimeUnit timeUnit, Closure change) {

		executor.schedule({ ->
			queueUpdate(change)
		}, delay, timeUnit)
	}

	/**
	 * Apply modifications to the scene that were queued with calls to
	 * {@link #queueUpdate(Closure)}.
	 */
	void update() {

		while (updateQueue) {
			updateQueue.poll().call()
		}
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
