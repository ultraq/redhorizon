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

package nz.net.ultraq.redhorizon.engine.physics

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.scenegraph.Node

/**
 * Base class for all collision nodes.
 *
 * @author Emanuel Rabina
 */
abstract class Collider<T extends Collider, S> extends Node<T> implements EventTarget<T> {

	/**
	 * Check whether this object is colliding with another.
	 */
	abstract boolean checkCollision(Collider other)

	/**
	 * Return a shape describing the collision space.
	 */
	abstract S getBounds()
}
