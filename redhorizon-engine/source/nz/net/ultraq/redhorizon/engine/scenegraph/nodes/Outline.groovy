/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type

import org.joml.Vector2f
import org.joml.primitives.Rectanglef

/**
 * Draws a line at the bounds of its parent node.  Mainly used for debugging.
 *
 * @author Emanuel Rabina
 */
class Outline extends Primitive {

	Outline(Rectanglef bounds, Colour colour, boolean dynamic = false) {

		super(Type.LINE_LOOP, colour, bounds as Vector2f[], dynamic)
		this.bounds { ->
			set(bounds)
		}
	}
}
