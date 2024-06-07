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

	final Matrix4f projection
	// TODO: Maybe don't need this any more as Nodes expose a transform matrix we
	//       can use instead
	final Matrix4f view = new Matrix4f()
	protected boolean moved = false

	private UniformBuffer viewProjectionBuffer

	/**
	 * Constructor, build a camera to work with the given dimensions.
	 */
	Camera(Dimension size) {

		projection = new Matrix4f()
			.ortho2D(-size.width() / 2, size.width() / 2, -size.height() / 2, size.height() / 2)
			.lookAt(
				new Vector3f(0, 0, 1),
				new Vector3f(),
				new Vector3f(0, 1, 0)
			)
		logger.debug('Establishing an orthographic projection of {}x{}', size.width(), size.height())
	}

	/**
	 * Move the camera so it centers the given point in the view.
	 */
	Camera center(float x, float y, float z = 0) {

		view.translate(view.origin(new Vector3f()).sub(x, y, z))
		moved = true
		return this
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
	 * Reset this camera's scale.
	 */
	Camera resetScale() {

		var scale = view.getScale(new Vector3f())
		view.scaleLocal(1 / scale.x as float, 1 / scale.y as float, 1)
		moved = true
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
		var scale = view.getScale(new Vector3f()).x
		var diff = factor / scale as float

		view.scaleLocal(diff, diff, 1)
		moved = true
		return this
	}

	/**
	 * Translates the position of this camera.
	 */
	Camera translate(float x, float y, float z = 0) {

		var scale = view.getScale(new Vector3f())
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
