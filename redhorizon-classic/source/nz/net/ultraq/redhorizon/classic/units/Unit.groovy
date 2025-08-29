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
import nz.net.ultraq.redhorizon.classic.nodes.Layer
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.nodes.Rotatable
import nz.net.ultraq.redhorizon.classic.shaders.Shaders
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteMeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.PartitionHint
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.graphics.Mesh
import nz.net.ultraq.redhorizon.graphics.Shader
import static nz.net.ultraq.redhorizon.classic.maps.Map.TILE_HEIGHT
import static nz.net.ultraq.redhorizon.classic.maps.Map.TILE_WIDTH

import org.joml.Vector3f

import java.util.concurrent.CompletableFuture

/**
 * A unit is a controllable object in the game.  As part of the Explorer
 * project, it is only stationary and used for showcasing the various
 * animations/states that it has.
 *
 * @author Emanuel Rabina
 */
class Unit extends Node<Unit> implements FactionColours, Rotatable {

	private static final int FRAMERATE = 10 // C&C ran animations at 10fps?

	/**
	 * The state value used for the not-animating / doing nothing state
	 */
	static final String DEFAULT_STATE = "default"

	final PartitionHint partitionHint = PartitionHint.SMALL_AREA

	// TODO: Should this type of file be renamed to better reflect its purpose?
	final ImagesFile imagesFile
	final UnitData unitData
	final UnitBody body
	final UnitTurret turret
	UnitBody body2
	PalettedSprite bib
	UnitShadow shadow

	private int stateIndex = 0
	private float accAnimationTime
	private SpriteSheet spriteSheet

	Unit(ImagesFile imagesFile, UnitData unitData) {

		this.imagesFile = imagesFile
		this.unitData = unitData

		bounds { ->
			setMax(imagesFile.width, imagesFile.height)
		}

		body = new UnitBody(imagesFile.width, imagesFile.height, imagesFile.numImages, { _ ->
			return CompletableFuture.completedFuture(spriteSheet)
		}, unitData)
		addChild(body)

		if (unitData.shpFile.parts.turret) {
			turret = new UnitTurret(imagesFile.width, imagesFile.height, imagesFile.numImages, { _ ->
				return CompletableFuture.completedFuture(spriteSheet)
			}).tap {
				layer = Layer.UP_ONE
			}
			addChild(turret)
		}
		else {
			turret = null
		}
	}

	/**
	 * TODO: This method signature sucks 😅  Need a better way to add components
	 *       like this to a unit.  Time to revisit ECS stuffs...
	 */
	void addBib(ImagesFile bibFile) {

		var structureWidthInCells = Math.ceil(width / TILE_WIDTH) as int

		var bibImageData = bibFile.imagesData.combine(bibFile.width, bibFile.height, bibFile.format, structureWidthInCells)
		var bibWidth = TILE_WIDTH * structureWidthInCells
		var bibHeight = TILE_HEIGHT * 2

		bib = new PalettedSprite(bibWidth, bibHeight, 1, 1f, 1f, { scene ->
			return scene.requestCreateOrGet(new SpriteSheetRequest(bibWidth, bibHeight, ColourFormat.FORMAT_INDEXED, bibImageData))
		})
		bib.name = "Bib"
		bib.setPosition(0, -TILE_HEIGHT)
		bib.layer = Layer.DOWN_THREE
		bib.partitionHint = PartitionHint.SMALL_AREA

		addChild(bib)
	}

	/**
	 * Adds a second body to this unit.  Used for special cases like the weapons
	 * factory where the garage door is a separate sprite file.
	 */
	void addBody(ImagesFile imagesFile, UnitData unitData) {

		body2 = new UnitBody(imagesFile, unitData).tap {
			name = "UnitBody2"
			layer = Layer.UP_ONE
		}
		addChild(body2)
	}

	/**
	 * Add a generated shadow to this unit.
	 *
	 * TODO: This is a good candidate for a component in an ECS.
	 */
	void addShadow() {

		shadow = new UnitShadow().tap {
			layer = Layer.DOWN_ONE
		}
		addChild(shadow)
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
	CompletableFuture<Void> onSceneAddedAsync(Scene scene) {

		return scene
			.requestCreateOrGet(new SpriteSheetRequest(imagesFile.width, imagesFile.height, imagesFile.format, imagesFile.imagesData))
			.thenAcceptAsync { newSpriteSheet ->
				spriteSheet = newSpriteSheet
			}
	}

	@Override
	CompletableFuture<Void> onSceneRemovedAsync(Scene scene) {

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

		accAnimationTime = 0
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

			super(width, height, numImages, 1f, 1f, spriteSheetGenerator)
			this.unitData = unitData
		}

		@Override
		void update(float delta) {

			// Update region in spritesheet to match heading and currently-playing animation
			if (spriteSheet) {
				var currentState = unitData.shpFile.states[stateIndex]
				var headings = currentState.headings
				var frames = currentState.frames
				var degreesPerHeading = 360f / headings

				// NOTE: C&C unit headings were ordered in a counter-clockwise order
				//       (maybe to match how radians work?), the reverse from how
				//       degrees-based headings are done.
				var closestHeading = Math.round(heading / degreesPerHeading)
				var rotationFrame = closestHeading ? (headings - closestHeading) * frames as int : 0
				var animationFrame = frames > 1 ? Math.floor((float)(accAnimationTime * FRAMERATE)) % frames as int : 0
				frame = unitData.shpFile.getStateFramesOffset(currentState) + rotationFrame + animationFrame
			}

			accAnimationTime += delta
			super.update(delta)
		}
	}

	/**
	 * Script to control the sprite representing the unit's turret.
	 */
	private class UnitTurret extends PalettedSprite {

		UnitTurret(int width, int height, int numImages, SpriteSheetGenerator spriteSheetGenerator) {
			super(width, height, numImages, 1f, 1f, spriteSheetGenerator)
		}

		@Override
		void update(float delta) {

			if (spriteSheet) {
				var turretHeadings = unitData.shpFile.parts.turret.headings
				var closestTurretHeading = Math.round(heading / degreesPerHeading)
				var turretRotationFrame = closestTurretHeading ? turretHeadings - closestTurretHeading as int : 0
				frame = unitData.shpFile.parts.body.headings + turretRotationFrame
			}

			super.update(delta)
		}
	}

	/**
	 * A generated unit shadow.  Used mainly for aircraft which draw a silhouette
	 * of the unit on the ground beneath them.
	 */
	private class UnitShadow extends Node<UnitShadow> implements GraphicsElement {

		private static final Vector3f offset = new Vector3f(0f, -20f, 0f)

		final String name = 'Shadow'

		private Mesh mesh
		private Shader shader

		UnitShadow() {

			bounds { ->
				set(body.bounds)
			}
			transform { ->
				translate(offset)
			}
		}

		@Override
		CompletableFuture<Void> onSceneAddedAsync(Scene scene) {

			return CompletableFuture.allOf(
				scene.requestCreateOrGet(new SpriteMeshRequest(bounds, spriteSheet.textureRegion))
					.thenAcceptAsync { newMesh ->
						mesh = newMesh
					},
				scene.requestCreateOrGet(new ShaderRequest(Shaders.shadowShader))
					.thenAcceptAsync { shadowShader ->
						shader = shadowShader
					}
			)
		}

		@Override
		CompletableFuture<Void> onSceneRemovedAsync(Scene scene) {

			return scene.requestDelete(mesh)
		}

		@Override
		void render(GraphicsRenderer renderer) {

			if (mesh && shader && body.material) {
				renderer.draw(mesh, globalTransform, shader, body.material)
			}
		}
	}
}
