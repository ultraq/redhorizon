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

package nz.net.ultraq.redhorizon.runtime.objects

import nz.net.ultraq.redhorizon.physics.BoxCollider
import nz.net.ultraq.redhorizon.scenegraph.Node

import org.joml.primitives.Rectanglef

/**
 * Colliders created at the edges of the screen to keep objects within the
 * screen.
 *
 * @author Emanuel Rabina
 */
class ScreenEdges extends Node<ScreenEdges> {

	public static final String TOP_COLLIDER_NAME = 'Top'
	public static final String BOTTOM_COLLIDER_NAME = 'Bottom'
	public static final String LEFT_COLLIDER_NAME = 'Left'
	public static final String RIGHT_COLLIDER_NAME = 'Right'

	/**
	 * Constructor, create the colliders at the edges of the specified bounds.
	 */
	ScreenEdges(Rectanglef bounds) {

		addChild(new BoxCollider(bounds.lengthX(), 1)
			.translate(0f, bounds.maxY)
			.withName(TOP_COLLIDER_NAME))
		addChild(new BoxCollider(bounds.lengthX(), 1)
			.translate(0f, bounds.minY)
			.withName(BOTTOM_COLLIDER_NAME))
		addChild(new BoxCollider(1, bounds.lengthY())
			.translate(bounds.minX, 0f)
			.withName(LEFT_COLLIDER_NAME))
		addChild(new BoxCollider(1, bounds.lengthY())
			.translate(bounds.maxX, 0f)
			.withName(RIGHT_COLLIDER_NAME))
	}
}
