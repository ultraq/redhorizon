/*
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.audio

import nz.net.ultraq.redhorizon.engine.geometry.Orientation
import nz.net.ultraq.redhorizon.engine.scenegraph.Node

import org.joml.Vector3f

/**
 * The player's ears into the game world.
 *
 * @author Emanuel Rabina
 */
class Listener extends Node<Listener> implements AudioElement {

	final Vector3f velocity = new Vector3f(0, 0, 0)
	final Orientation orientation = new Orientation()

	/**
	 * Sets up initial listener properties, like volume.
	 */
	@Override
	void init(AudioRenderer renderer) {

		renderer.updateVolume(1.0)
	}

	/**
	 * Update the listener in the environment.
	 *
	 * @param renderer
	 */
	@Override
	void render(AudioRenderer renderer) {

		renderer.updateListener(position, velocity, orientation)
	}
}
