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
	protected List<Node> children = []

	private final Rectanglef globalBounds = new Rectanglef()
	private final Matrix4f globalTransform = new Matrix4f()
	private final Vector3f globalScale = new Vector3f()
	private final Vector3f globalTranslate = new Vector3f()

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
	 * Return the world-space bounds of this node.  ie: the local bounds, then
	 * taking into account local and all parent/ancestor transforms along the path
	 * to this node.
	 *
	 * @return
	 */
	// TODO: Surely this is inefficient having to calculate this each time? 🤔
	Rectanglef getGlobalBounds() {

		getGlobalTransform()
		var scale = globalTransform.getScale(globalScale)
		var translate = globalTransform.getTranslation(globalTranslate)
		return globalBounds.set(bounds)
			.scale(scale.x, scale.y)
			.translate(translate.x, translate.y)
	}

	/**
	 * Get the world-space transform of this node.  ie: the local transform, then
	 * modified by all of the ancestor transforms along the path to this node.
	 * The result is stored in the private {@code globalTransform} property, and
	 * returned.
	 *
	 * @return
	 */
	// TODO: Surely this is inefficient having to calculate this each time? 🤔
	private Matrix4f getGlobalTransform() {

		return parent != null ?
			globalTransform.mul(parent.globalTransform) :
			globalTransform.set(transform)
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
