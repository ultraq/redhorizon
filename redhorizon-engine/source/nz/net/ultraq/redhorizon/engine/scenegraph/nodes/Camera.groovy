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

import nz.net.ultraq.redhorizon.engine.game.GameObject
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.UniformBufferRequest
import nz.net.ultraq.redhorizon.engine.graphics.UniformBuffer
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3fc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.system.MemoryStack.stackPush

import java.util.concurrent.CompletableFuture

/**
 * A representation of the player's view into the world.
 *
 * @author Emanuel Rabina
 */
class Camera extends Node<Camera> implements GameObject, GraphicsElement {

	private static final Logger logger = LoggerFactory.getLogger(Camera)

	private final int[] viewportDef
	private final Matrix4f projection
	private final Matrix4f view
	private final Matrix4f viewTransform = new Matrix4f()
	private final Matrix4f viewProjection = new Matrix4f()
	private UniformBuffer viewProjectionBuffer
	private final Vector3f inverse = new Vector3f()
	private final Vector3f projectionCalc = new Vector3f()

	private Node tracking

	/**
	 * Constructor, build a camera to work with the given dimensions.
	 */
	Camera(Dimension size) {

		viewportDef = [0, 0, size.width(), size.height()]
		projection = new Matrix4f().setOrthoSymmetric(size.width(), size.height(), 0f, 10f)
		logger.debug('Establishing an orthographic projection of {}x{}', size.width(), size.height())

		view = new Matrix4f().setLookAt(
			0, 0, 10,
			0, 0, 0,
			0, 1, 0
		)
	}

	/**
	 * Convert an object's 3D position in the scene to a 2D position on the
	 * screen.
	 */
	Vector2f calculateSceneToScreenSpace(Vector3fc position, Vector2f dest) {

		getViewProjection().project(position, viewportDef, projectionCalc)
		return dest.set(projectionCalc.x(), projectionCalc.y())
	}

	/**
	 * Make the camera follow another object around the scene.
	 */
	void follow(Node node) {

		tracking = node
	}

	@Override
	Vector3f getPosition() {

		// Positioning the camera is the opposite of what we would expect as we are
		// instead creating a transform matrix that moves the world around it
		return super.getPosition().negate(inverse)
	}

	/**
	 * Return a matrix that is the view * local transform of this camera.  Note
	 * that the returned matrix is a live value, so be sure to wrap in your own
	 * object if you need a stable value.
	 */
	private Matrix4f getView() {

		return view.mulAffine(transform, viewTransform)
	}

	/**
	 * Return a matrix that is the projection * view * local transform of this
	 * camera.  Note that the returned matrix is a live value, so be sure to wrap
	 * in your own object if you need a stable value.
	 */
	Matrix4f getViewProjection() {

		return projection.mulOrthoAffine(getView(), viewProjection)
	}

	@Override
	CompletableFuture<Void> onSceneAddedAsync(Scene scene) {

		return CompletableFuture.supplyAsync { ->
			return stackPush().withCloseable { stack ->
				return getView().get(Matrix4f.FLOATS, projection.get(0, stack.mallocFloat(Matrix4f.FLOATS * 2)))
			}
		}
			.thenComposeAsync { data ->
				return scene.requestCreateOrGet(new UniformBufferRequest('Camera', data, true))
			}
			.thenAcceptAsync { newUniformBuffer ->
				viewProjectionBuffer = newUniformBuffer
			}
	}

	@Override
	CompletableFuture<Void> onSceneRemovedAsync(Scene scene) {

		return scene.requestDelete(viewProjectionBuffer)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (viewProjectionBuffer) {
			stackPush().withCloseable { stack ->
				viewProjectionBuffer.updateBufferData(getView().get(stack.mallocFloat(Matrix4f.FLOATS)), Matrix4f.BYTES)
			}
		}
	}

	/**
	 * Reset this camera's position.
	 */
	void reset() {

		transform.identity()
		recalculateProperties()
	}

	@Override
	void setPosition(float x, float y, float z = position.z()) {

		// Positioning the camera is the opposite of what we would expect as we are
		// instead creating a transform matrix that moves the world around it
		super.setPosition(-x, -y, -z)
	}

	@Override
	void update(float delta) {

		if (tracking) {
			setPosition(tracking.globalPosition.x(), tracking.globalPosition.y())
		}
	}
}
