/*
 * Copyright 2017, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph

import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Scriptable

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.primitives.Rectanglef

/**
 * An element of a scene, nodes are used to build and organize scene trees.
 *
 * @author Emanuel Rabina
 */
class Node<T extends Node> implements SceneEvents, Scriptable<T>, Visitable {

	final Vector3f position = new Vector3f()
	final Matrix4f transform = new Matrix4f()
	final Rectanglef bounds = new Rectanglef()

	protected Node parent
	protected final List<Node> children = []

	@Override
	void accept(SceneVisitor visitor) {

		visitor.visit(this)
		children*.accept(visitor)
	}

	/**
	 * Adds a child node to this node.
	 *
	 * @param child
	 * @return
	 */
	T addChild(Node child) {

		children << child
		child.parent = this
		return this
	}

	/**
	 * An alias for {@link #addChild(Node)}
	 *
	 * @param child
	 */
	void leftShift(Node child) {

		addChild(child)
	}

	/**
	 * Default implementation of the scene added event to notify any attached
	 * script, then this node's children.
	 *
	 * @param scene
	 */
	@Override
	void onSceneAdded(Scene scene) {

		script?.onSceneAdded(scene)
		children.each { child ->
			child.onSceneAdded(scene)
		}
	}
}
