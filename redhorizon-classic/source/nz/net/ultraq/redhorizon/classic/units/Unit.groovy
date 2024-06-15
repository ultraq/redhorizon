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

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.nodes.FactionColours
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.nodes.Rotatable
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import java.util.concurrent.CompletableFuture

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
	final UnitData unitData
	final UnitBody body
	final UnitTurret turret
	UnitBody body2

	private int stateIndex = 0
	private long animationStartTime
	private SpriteSheet spriteSheet

	Unit(ImagesFile imagesFile, UnitData unitData) {

		this.imagesFile = imagesFile
		this.unitData = unitData

		bounds.setMax(imagesFile.width, imagesFile.height)

		body = new UnitBody(imagesFile.width, imagesFile.height, imagesFile.numImages, { _ ->
			return CompletableFuture.completedFuture(spriteSheet)
		}, unitData)
		addChild(body)

		if (unitData.shpFile.parts.turret) {
			turret = new UnitTurret(imagesFile.width, imagesFile.height, imagesFile.numImages, { _ ->
				return CompletableFuture.completedFuture(spriteSheet)
			})
			addChild(turret)
		}
		else {
			turret = null
		}
	}

	/**
	 * Adds a second body to this unit.  Used for special cases like the weapons
	 * factory where the garage door is a separate sprite file.
	 */
	void addBody(ImagesFile imagesFile, UnitData unitData) {

		body2 = new UnitBody(imagesFile, unitData).tap {
			name = "UnitBody2"
		}
		addChild(body2)
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
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return scene
			.requestCreateOrGet(new SpriteSheetRequest(imagesFile.width, imagesFile.height, imagesFile.format, imagesFile.imagesData))
			.thenAcceptAsync { newSpriteSheet ->
				spriteSheet = newSpriteSheet
			}
	}

	@Override
	CompletableFuture<Void> onSceneRemoved(Scene scene) {

		return scene.requestDelete(spriteSheet)
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

	@Override
	void setFaction(Faction faction) {

		FactionColours.super.faction = faction
		body.faction = faction
		body2?.faction = faction
		turret?.faction = faction
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

	/**
	 * Script to control the sprite representing the unit's main body.
	 */
	private class UnitBody extends PalettedSprite {

		private final UnitData unitData

		UnitBody(ImagesFile imagesFile, UnitData unitData) {

			super(imagesFile)
			this.unitData = unitData
		}

		UnitBody(int width, int height, int numImages, SpriteSheetGenerator spriteSheetGenerator, UnitData unitData) {

			super(width, height, numImages, spriteSheetGenerator)
			this.unitData = unitData
		}

		@Override
		void update() {

			// TODO: If this animation region picking gets more complicated, it might
			//       be worth making an 'animation library' for units

			if (spriteSheet) {
				// Update region in spritesheet to match heading and currently-playing animation
				var currentState = unitData.shpFile.states[stateIndex]
				var headings = currentState.headings
				var frames = currentState.frames
				var degreesPerHeading = 360f / headings

				// NOTE: C&C unit headings were ordered in a counter-clockwise order, the
				//       reverse from how we normally define rotation.
				var closestHeading = Math.round(heading / degreesPerHeading)
				var rotationFrame = closestHeading ? (headings - closestHeading) * frames as int : 0
				var animationFrame = frames ? Math.floor((currentTimeMs - animationStartTime) / 1000 * FRAMERATE) % frames as int : 0
				region.set(spriteSheet.getFrame(unitData.shpFile.getStateFramesOffset(currentState) + rotationFrame + animationFrame))
			}

			super.update()
		}
	}

	/**
	 * Script to control the sprite representing the unit's turret.
	 */
	private class UnitTurret extends PalettedSprite {

		UnitTurret(int width, int height, int numImages, SpriteSheetGenerator spriteSheetGenerator) {
			super(width, height, numImages, spriteSheetGenerator)
		}

		@Override
		void update() {

			if (spriteSheet) {
				var turretHeadings = unitData.shpFile.parts.turret.headings
				var closestTurretHeading = Math.round(heading / degreesPerHeading)
				var turretRotationFrame = closestTurretHeading ? turretHeadings - closestTurretHeading as int : 0
				region.set(spriteSheet.getFrame(unitData.shpFile.parts.body.headings + turretRotationFrame))
			}

			super.update()
		}
	}
}
