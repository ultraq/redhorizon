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
		includes = ['clear', 'insertBefore', 'leftShift', 'removeChild', 'rotate', 'scale', 'translate', 'traverse'],
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
	 * Gather all nodes in the scene of the given type.
	 *
	 * @param type
	 * @param results
	 *   Optional, provide a collection to store the results in to avoid
	 *   allocations.
	 * @return The {@code results} list.
	 */
	<T extends Node> List<T> collect(Class<T> type, List<T> results = []) {

		root.traverse { Node node ->
			if (type.isInstance(node)) {
				results << (T)node
			}
			return true
		}
		return results
	}

	/**
	 * Locate the first descendent from this node that satisfies the given
	 * predicate.  Prefer to use the {@code find} methods for node name or class
	 * for better performance.
	 *
	 * @return The matching node, or {@code null} if no match is found.
	 */
	<T extends Node> T find(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.scenegraph.Node')
			Closure<Boolean> predicate) {

		return root.find(predicate)
	}

	/**
	 * Locate the first descendent from this node that satisfies the given name.
	 *
	 * @return The matching node, or {@code null} if no match is found.
	 */
	<T extends Node> T find(String name) {

		return root.find(name)
	}

	/**
	 * Locate the first descendent from this node that satisfies the given type.
	 *
	 * @return The matching node, or {@code null} if no match is found.
	 */
	<T extends Node> T find(Class<T> type) {

		return root.find(type)
	}

	/**
	 * Locate every descendant of this node that satisfies the given predicate.
	 * Prefer to use the {@code findAll} methods for a node class for better
	 * performance.
	 *
	 * @param results
	 *   Optional, a list to hold the results so that a new list isn't allocated.
	 * @return The matching nodes, or an empty list if no matches are found.
	 */
	<T extends Node> List<T> findAll(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.scenegraph.Node')
			Closure<Boolean> predicate,
		List<T> results = []) {

		return root.findAll(predicate, results)
	}

	/**
	 * Locate every descendant of this node that satisfies the given type.
	 *
	 * @param results
	 *   Optional, a list to hold the results so that a new list isn't allocated.
	 * @return The matching nodes, or an empty list if no matches are found.
	 */
	<T extends Node> List<T> findAll(Class<T> type, List<T> results = []) {

		return root.findAll(type, results)
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
	private class RootNode extends Node {

		@Override
		Scene getScene() {

			return Scene.this
		}
	}
}
