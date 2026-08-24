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

package nz.net.ultraq.redhorizon.physics

import nz.net.ultraq.redhorizon.scenegraph.Node

import org.joml.Vector2f

import groovy.transform.TupleConstructor

/**
 * A node containing properties describing movement and trajectory.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(includes = ['maxSpeed', 'vector'])
class MovementNode extends Node<MovementNode> {

	/**
	 * The current speed of the node.
	 */
	float speed

	/**
	 * The maximum speed at which the node can move.
	 */
	float maxSpeed

	/**
	 * The rate at which it takes to reach {@code maxSpeed} from a standstill.  0
	 * means that the node will reach full speed instantly.
	 */
//	float acceleration

	/**
	 * The intended direction and intensity of the movement, ranging from 0 to 1
	 * or -1 on each axis.
	 */
	final Vector2f vector = new Vector2f()

	/**
	 * The last vector value applied to the node before it was removed.
	 */
//	final Vector2f lastVector = new Vector2f()

	/**
	 * The current speed and direction of the motion.
	 */
	final Vector2f velocity = new Vector2f()
}
