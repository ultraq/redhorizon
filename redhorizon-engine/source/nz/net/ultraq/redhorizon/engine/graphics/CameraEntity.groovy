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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Window

import org.joml.Matrix4fc
import org.joml.Vector3f

/**
 * An entity with just the camera attached.
 *
 * @author Emanuel Rabina
 */
class CameraEntity extends Entity<CameraEntity> {

	final String name = 'Camera'
	final Camera camera

	/**
	 * Constructor, bakes a camera component into this entity.
	 */
	CameraEntity(int sceneWidth, int sceneHeight, Window window) {

		camera = addAndReturnComponent(new CameraComponent(sceneWidth, sceneHeight, window)).camera
	}

	/**
	 * Return a view-projection matrix for the camera.
	 */
	Matrix4fc getViewProjection() {

		return camera.getViewProjection(transform)
	}

	/**
	 * Updates rendering with the camera state.
	 */
	void render(SceneShaderContext sceneShaderContext) {

		camera.render(sceneShaderContext, transform)
	}

	/**
	 * Convert a set of window coordinates to world coordinates.
	 */
	Vector3f unproject(float winX, float winY, Vector3f result) {

		return camera.unproject(winX, winY, transform, result)
	}
}
