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

package nz.net.ultraq.redhorizon.jomlextensions

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * Static primitive size constants for the JOML classes.
 *
 * @author Emanuel Rabina
 */
class ConstantsExtensions {

	private static final Matrix4fc IDENTITY = new Matrix4f()
	private static final Vector2fc UP = new Vector2f(0f, 1f)

	/**
	 * The number of floats used to represent a {@code Matrix4f}.
	 */
	static int getFLOATS(Matrix4f self) {

		return 16
	}

	/**
	 * The number of floats used to represent a {@code Vector2f}.
	 */
	static int getFLOATS(Vector2f self) {

		return 2
	}

	/**
	 * The number of floats used to represent a {@code Vector3f}.
	 */
	static int getFLOATS(Vector3f self) {

		return 3
	}

	/**
	 * The number of floats used to represent a {@code Vector4f}.
	 */
	static int getFLOATS(Vector4f self) {

		return 4
	}

	/**
	 * Return a read-only identity matrix.
	 */
	static Matrix4fc getIDENTITY(Matrix4f self) {

		return IDENTITY
	}

	/**
	 * Return a reusable vector for the 'up' direction in this game.
	 */
	static Vector2fc getUP(Vector2f self) {

		return UP
	}
}
