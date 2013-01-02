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

package redhorizon.engine.audio;

import redhorizon.geometry.Orientation;
import redhorizon.geometry.Ray;
import redhorizon.geometry.Vector3f;
import redhorizon.scenegraph.BoundingBox;
import redhorizon.scenegraph.BoundingVolume;
import redhorizon.scenegraph.Spatial;

/**
 * The player's 'ears' in the world.
 * 
 * @author Emanuel Rabina
 */
public class Listener extends Spatial {

	private Vector3f velocity = new Vector3f();
	private Orientation orientation = new Orientation();

	/**
	 * Constructor, sets up the listener using the default settings.
	 */
	Listener() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoundingVolume boundingVolume() {

		return BoundingBox.ZERO;
	}

	/**
	 * Return the current orientation of this listener.
	 * 
	 * @return Listener orientation.
	 */
	public Orientation getOrientation() {

		return orientation;
	}

	/**
	 * Return the current velocity of this listener.
	 * 
	 * @return Listener velocity.
	 */
	public Vector3f getVelocity() {

		return velocity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean intersects(Ray ray) {

		return false;
	}

	/**
	 * Set the listener in the environment.
	 * 
	 * @param renderer
	 */
	public void render(AudioRenderer renderer) {

		renderer.updateListener(this);
	}
}
