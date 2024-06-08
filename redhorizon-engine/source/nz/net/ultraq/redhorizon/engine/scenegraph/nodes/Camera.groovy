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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.UniformBufferRequest
import nz.net.ultraq.redhorizon.engine.graphics.UniformBuffer
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.Matrix4f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.system.MemoryStack.stackPush

import java.util.concurrent.CompletableFuture

/**
 * A representation of the player's view into the world.
 *
 * @author Emanuel Rabina
 */
class Camera extends Node<Camera> {

	private static final Logger logger = LoggerFactory.getLogger(Camera)

	private final Matrix4f projection
	private final Matrix4f view
	private final Vector3f position = new Vector3f()
	private final Vector3f scale = new Vector3f()
	private boolean moved = false

	private UniformBuffer viewProjectionBuffer

	/**
	 * Constructor, build a camera to work with the given dimensions.
	 */
	Camera(Dimension size) {

		projection = new Matrix4f().setOrtho2D(
			-size.width() / 2, size.width() / 2,
			-size.height() / 2, size.height() / 2
		)
		logger.debug('Establishing an orthographic projection of {}x{}', size.width(), size.height())

		view = new Matrix4f().setLookAt(
			0, 0, 1,
			0, 0, 0,
			0, 1, 0
		)
	}

	/**
	 * Return the camera's current position, setting the result in {@code dest}.
	 */
	Vector3f getPosition(Vector3f dest) {

		return view.origin(dest)
	}

	/**
	 * Return the camera's current scale, setting the result in {@code dest}.
	 */
	Vector3f getScale(Vector3f dest) {

		return view.getScale(dest)
	}

	/**
	 * Return a matrix that is the view * projection of this camera, setting the
	 * result in {@code dest}.
	 */
	Matrix4f getViewProjection(Matrix4f dest) {

		return projection.mulOrthoAffine(view, dest)
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return CompletableFuture.supplyAsync { ->
			return stackPush().withCloseable { stack ->
				return view.get(Matrix4f.FLOATS, projection.get(0, stack.mallocFloat(Matrix4f.FLOATS * 2)))
			}
		}
			.thenComposeAsync { data ->
				return scene.requestCreateOrGet(new UniformBufferRequest('Camera', data))
			}
			.thenAcceptAsync { newUniformBuffer ->
				viewProjectionBuffer = newUniformBuffer
			}
	}

	/**
	 * Move the camera to the given position.
	 */
	Camera position(Vector3f point) {

		view.translate(getPosition(this.position).sub(point))
		moved = true
		return this
	}

	/**
	 * Reset this camera's properties.
	 */
	Camera reset() {

		view.setLookAt(
			0, 0, 1,
			0, 0, 0,
			0, 1, 0
		)
		return this
	}

	/**
	 * Scale the camera view as a way of altering the perceived zoom.
	 *
	 * @param factor
	 *   Amount to scale relative to the standard zoom.
	 * @return
	 */
	Camera scale(float factor) {

		// Calculate diff between current and target scale to use as the scale factor
		var scale = getScale(this.scale).x
		var diff = factor / scale as float

		view.scaleLocal(diff, diff, 1)
		moved = true
		return this
	}

	/**
	 * Translates the position of this camera.
	 */
	Camera translate(float x, float y, float z = 0) {

		var scale = view.getScale(scale)
		view.translate(x / scale.x as float, y / scale.y as float, z)
		moved = true
		return this
	}

	/**
	 * Update the camera's matrices before rendering.
	 *
	 * @return
	 * {@code true} if the camera had to perform operations because it had been
	 *   moved.
	 */
	void update() {

		if (moved) {
			stackPush().withCloseable { stack ->
				viewProjectionBuffer.updateBufferData(view.get(stack.mallocFloat(Matrix4f.FLOATS)), Matrix4f.BYTES)
			}
			moved = false
		}
	}
}
