/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.input

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.scenegraph.Node

import org.joml.primitives.Rayf
import org.joml.primitives.Rectanglef

import groovy.transform.TupleConstructor

/**
 * A node which can be selected by the player.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class Selectable<T extends Selectable<T>> extends Node<T> implements EventTarget<T> {

	final float width
	final float height
	private final Rectanglef bounds = new Rectanglef(0, 0, width, height)

	/**
	 * Return whether or not this node is intersected by a ray, usually
	 * representing a cursor.
	 *
	 * <p>Note that this is only for 2D scenes as the calculation will just take
	 * the ray's origin X/Y values and see if they exist inside the box that makes
	 * up this selectable node.
	 */
	boolean intersectsRay(Rayf ray) {

		var position = globalPosition
		return bounds.center().translate(position.x(), position.y()).containsPoint(ray.oX, ray.oY)
	}
}
