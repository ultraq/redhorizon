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

package nz.net.ultraq.redhorizon.scenegraph

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Rectanglef

/**
 * Interface for an element that can be a part of a scene.
 * 
 * @param <T>
 * @author Emanuel Rabina
 */
trait SceneElement<T extends SceneElement> implements Visitable {

	final Matrix4f transform = new Matrix4f()
	final Rectanglef bounds = new Rectanglef()

	@Override
	void accept(SceneVisitor visitor) {

		visitor.visit(this)
	}

	/**
	 * Scale the X and Y values of this element.
	 * 
	 * @param factor
	 * @return
	 */
	T scaleXY(float factor) {

		transform.scaleXY(factor, factor)
		bounds.scale(factor)
		return this
	}

	/**
	 * Translate the position of this element.
	 * 
	 * @param offset
	 * @return
	 */
	T translate(Vector3f offset) {

		return translate(offset.x, offset.y, offset.z)
	}

	/**
	 * Translate the position of this element.
	 * 
	 * @param xyOffset
	 * @param z
	 * @return
	 */
	T translate(Vector2f xyOffset, float z = 0) {

		return translate(xyOffset.x, xyOffset.y, z)
	}

	/**
	 * Translate the position of this element.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	T translate(float x, float y, float z = 0) {

		transform.translate(x, y, z)
		bounds.translate(x, y)
		return this
	}
}
