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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.geometry.Orientation
import nz.net.ultraq.redhorizon.engine.scenegraph.AudioElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node

import org.joml.Vector3f
import org.joml.primitives.Circlef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * An object that can pick up audio within a certain hearing range.
 *
 * @author Emanuel Rabina
 */
class Listener extends Node<Listener> implements AudioElement {

	private static final Logger logger = LoggerFactory.getLogger(Listener)

	final Vector3f velocity = new Vector3f(0, 0, 0)
	final Orientation orientation = new Orientation()
	final Circlef range = new Circlef(0f, 0f, 100f) // TODO: Set this from the game

	private final float volume

	/**
	 * Constructor, build a listener with the given hearing sensitivity.
	 */
	Listener(float volume) {

		this.volume = volume
		logger.debug('Creating a listener w/ volume gain of {}%', volume * 100)
	}

	@Override
	void render(AudioRenderer renderer) {

		renderer.updateListener(volume, globalPosition)
	}
}
