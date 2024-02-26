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

package nz.net.ultraq.redhorizon.classic.units

import nz.net.ultraq.redhorizon.classic.nodes.FactionColours
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.nodes.Rotatable
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import groovy.transform.InheritConstructors
import java.nio.ByteBuffer

/**
 * A unit is a controllable object in the game.  As part of the Explorer
 * project, it is only stationary and used for showcasing the various
 * animations/states that it has.
 *
 * @author Emanuel Rabina
 */
class Unit extends Node<Unit> implements FactionColours, Rotatable, Temporal {

	private static final int FRAMERATE = 10 // C&C ran animations at 10fps?

	/**
	 * The state value used for the not-animating / doing nothing state
	 */
	static final String DEFAULT_STATE = "default"

	// TODO: Should this type of file be renamed to better reflect its purpose?
	final ImagesFile imagesFile
	final Palette palette
	final UnitData unitData

	private PalettedSprite body
	private PalettedSprite turret
	private int stateIndex = 0
	private long animationStartTime
	private SpriteSheet spriteSheet
	private Texture paletteAsTexture

	Unit(ImagesFile imagesFile, Palette palette, UnitData unitData) {

		this.imagesFile = imagesFile
		this.palette = palette
		this.unitData = unitData
	}

	/**
	 * Return the number of degrees it takes to rotate the unit left/right in
	 * either direction for the current state of the unit.
	 */
	private float getDegreesPerHeading() {

		return 360f / unitData.shpFile.states[stateIndex].headings
	}

	/**
	 * Return the name of the current state of the unit.
	 */
	String getState() {

		return unitData.shpFile.states[stateIndex].name
	}

	@Override
	void onSceneAdded(Scene scene) {

		var width = imagesFile.width
		var height = imagesFile.height

		// TODO: Load the palette once
		paletteAsTexture = scene
			.requestCreateOrGet(new TextureRequest(256, 1, palette.format, palette as ByteBuffer))
			.get()

		spriteSheet = scene
			.requestCreateOrGet(new SpriteSheetRequest(width, height, imagesFile.format, imagesFile.imagesData))
			.get()

		body = new UnitBody(width, height, spriteSheet, paletteAsTexture, spriteSheet.getFrame(0))
		addChild(body)

		if (unitData.shpFile.parts.turret) {
			turret = new UnitTurret(width, height, spriteSheet, paletteAsTexture, spriteSheet.getFrame(unitData.shpFile.parts.body.headings))
			addChild(turret)
		}
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(spriteSheet, paletteAsTexture)
	}

	/**
	 * Adjust the heading of the unit counter-clockwise enough to utilize its next
	 * state/animation in that direction.
	 */
	void rotateLeft() {

		heading -= degreesPerHeading
	}

	/**
	 * Adjust the heading of the unit clockwise enough to utilize its next
	 * state/animation in that direction.
	 */
	void rotateRight() {

		heading += degreesPerHeading
	}

	/**
	 * Put the unit into the given state.
	 */
	void setState(String state) {

		var foundStateIndex = unitData.shpFile.states.findIndexOf { it.name == state }
		if (foundStateIndex != -1) {
			stateIndex = foundStateIndex
		}
		else {
			stateIndex = 0
		}
	}

	/**
	 * (Re)start playing the current animation.
	 */
	void startAnimation() {

		animationStartTime = currentTimeMs
	}

	@InheritConstructors
	class UnitBody extends PalettedSprite {

		@Override
		void render(GraphicsRenderer renderer) {

			// TODO: If this animation region picking gets more complicated, it might
			//       be worth making an 'animation library' for units

			// Update region in spritesheet to match heading and currently-playing animation
			var currentState = unitData.shpFile.states[stateIndex]
			var headings = currentState.headings
			var frames = currentState.frames

			// NOTE: C&C unit headings were ordered in a counter-clockwise order, the
			//       reverse from how we normally define rotation.
			var closestHeading = Math.round(heading / degreesPerHeading)
			var rotationFrame = closestHeading ? (headings - closestHeading) * frames as int : 0
			var animationFrame = frames ? Math.floor((currentTimeMs - animationStartTime) / 1000 * FRAMERATE) % frames as int : 0
			region.set(spriteSheet.getFrame(unitData.shpFile.getStateFramesOffset(currentState) + rotationFrame + animationFrame))

			super.render(renderer)
		}
	}

	@InheritConstructors
	class UnitTurret extends PalettedSprite {

		@Override
		void render(GraphicsRenderer renderer) {

			var headings = unitData.shpFile.parts.turret.headings
			var closestHeading = Math.round(heading / degreesPerHeading)
			var rotationFrame = closestHeading ? headings - closestHeading as int : 0
			region.set(spriteSheet.getFrame(unitData.shpFile.parts.body.headings + rotationFrame))

			super.render(renderer)
		}
	}
}
