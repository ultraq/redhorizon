/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.engine.graphics;

import redhorizon.geometry.Ray;
import redhorizon.scenegraph.BoundingBox;
import redhorizon.scenegraph.BoundingVolume;
import redhorizon.scenegraph.Spatial;

/**
 * The players eyes into the world.
 * 
 * @author Emanuel Rabina
 */
public class Camera extends Spatial {

	/**
	 * Camera projection types.
	 */
	public static enum CameraProjection {

		ORTHOGRAPHIC,
		PERSPECTIVE;
	}

	private final CameraProjection projection;
	private final float width;
	private final float height;
	private final float depth;

	/**
	 * Constructor, sets-up a camera with the given projection type and
	 * viewing range.
	 * 
	 * @param projection
	 * @param width	 Viewing width of the camera.
	 * @param height Viewing height of the camera.
	 * @param depth	 Viewing depth (draw distance) of the camera.
	 */
	Camera(CameraProjection projection, float width, float height, float depth) {

		this.projection = projection;
		this.width  = width;
		this.height = height;
		this.depth  = depth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoundingVolume boundingVolume() {

		return BoundingBox.ZERO;
	}

	/**
	 * Returns this camera's projection type.
	 * 
	 * @return Camera projection type.
	 */
	public CameraProjection getProjectionType() {

		return projection;
	}

	/**
	 * Return this camera's viewing depth.
	 * 
	 * @return Viewing depth.
	 */
	public float getDepth() {

		return depth;
	}

	/**
	 * Return this camera's viewing height.
	 * 
	 * @return Viewing height.
	 */
	public float getHeight() {

		return height;
	}

	/**
	 * Return this camera's viewing width.
	 * 
	 * @return Viewing width.
	 */
	public float getWidth() {

		return width;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean intersects(Ray ray) {

		return false;
	}

	/**
	 * Set/update the camera in the environment.
	 * 
	 * @param renderer
	 */
	public void render(GraphicsRenderer renderer) {

		renderer.updateCamera(this);
	}
}
