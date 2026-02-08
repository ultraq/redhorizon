/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer.previews

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.graphics.FactionComponent
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * Any of the C&C unit types.
 *
 * @author Emanuel Rabina
 */
class UnitPreview extends Entity<UnitPreview> {

	private final UnitData unitData
	private float heading = 0f
	private int stateIndex = 0

	/**
	 * Constructor, create a new unit out of it's sprite and metadata.
	 */
	UnitPreview(SpriteSheet spriteSheet, UnitData unitData) {

		this.unitData = unitData

		addComponent(new FactionComponent(Faction.GOLD))
		addComponent(new SpriteComponent(spriteSheet, PalettedSpriteShader)
			.withName('Body'))
		if (unitData.shpFile.parts.turret) {
			addComponent(new SpriteComponent(spriteSheet, PalettedSpriteShader)
				.withName('Turret'))
		}
		addComponent(new ScriptNode(UnitPreviewScript))
	}

	/**
	 * The number of degrees that the unit needs to be rotated so that it displays
	 * a different frame of rotation.
	 *
	 * <p>We're choosing to use degrees over radians because the way the sprites
	 * are cut divides cleanly into 360, leading to better whole numbers when
	 * dealing with rotation frames.
	 */
	float getDegreesPerHeading() {

		return 360f / unitData.shpFile.states[stateIndex].headings
	}

	/**
	 * Move to the next set of sprite frames in an animation sequence.
	 */
	void nextAnimation() {

		stateIndex = Math.wrap(stateIndex + 1, 0, unitData.shpFile.states.length)
	}

	/**
	 * Move to the previous set of sprite frames in an animation sequence.
	 */
	void previousAnimation() {

		stateIndex = Math.wrap(stateIndex - 1, 0, unitData.shpFile.states.length)
	}

	/**
	 * Move to the next sprite frame that shows a left rotation.
	 */
	void rotateLeft() {

		heading = Math.wrapCircleDegrees(heading - degreesPerHeading as float)
	}

	/**
	 * Move to the next sprite frame that shows a right rotation.
	 */
	void rotateRight() {

		heading = Math.wrapCircleDegrees(heading + degreesPerHeading as float)
	}

	/**
	 * Showcase behaviours for a unit.
	 */
	static class UnitPreviewScript extends Script {

		private static final Logger logger = LoggerFactory.getLogger(UnitPreview)
		private static final int FRAMERATE = 10 // C&C ran animations at 10fps?
		private static final float repeatInterval = 0.1f

		private SpriteComponent sprite
		private float repeatTimer
		private float animationTimer

		@Override
		void init() {

			// Animate to 2x scale
//			var camera = (entity.scene as ExplorerScene).camera
//			var scalingResult = new Matrix4f()
//			new Transition(EasingFunctions::easeOutSine, 400, { float value ->
//				camera.setTransform(scalingResult.identity()
//					.scale(1f + value as float))
//			})
//				.start()
			(node.scene as ExplorerScene).camera.scale(2f)
			sprite = node.findComponentByType(SpriteComponent)
		}

		@Override
		void update(float delta) {

			repeatTimer += delta
			animationTimer += delta

			if (input.keyPressed(GLFW_KEY_A) && repeatTimer >= repeatInterval) {
				node.rotateLeft()
				repeatTimer = 0f
			}
			else if (input.keyPressed(GLFW_KEY_D) && repeatTimer >= repeatInterval) {
				node.rotateRight()
				repeatTimer = 0f
			}
			else if (input.keyPressed(GLFW_KEY_W, true)) {
				node.previousAnimation()
				logger.info('Showing {} state', node.unitData.shpFile.states[node.stateIndex].name)
				animationTimer = 0f
			}
			else if (input.keyPressed(GLFW_KEY_S, true)) {
				node.nextAnimation()
				logger.info('Showing {} state', node.unitData.shpFile.states[node.stateIndex].name)
				animationTimer = 0f
			}

			if (input.keyPressed(GLFW_KEY_F, true)) {
				var factions = Faction.values()
				var currentFaction = node.findComponentByType(FactionComponent)
				var nextFaction = factions[(currentFaction.faction.ordinal() + 1) % factions.length]
				currentFaction.faction = nextFaction
				logger.info('Viewing with {} faction colours', nextFaction.name())
			}

			var currentState = node.unitData.shpFile.states[node.stateIndex]
			var frames = currentState.frames
			var closestHeading = Math.round(node.heading / node.degreesPerHeading)
			var rotationFrame = closestHeading ? (currentState.headings - closestHeading) * frames as int : 0
			var animationFrame = frames > 1 ? Math.floor((float)(animationTimer * FRAMERATE)) % frames as int : 0
			var frame = node.unitData.shpFile.getStateFramesOffset(currentState) + rotationFrame + animationFrame
			sprite.framePosition.set(sprite.spriteSheet.getFramePosition(frame))

			var turret = node.findComponent { it.name == 'Turret' } as SpriteComponent
			if (turret) {
				var turretData = node.unitData.shpFile.parts.turret
				turret.framePosition.set(turret.spriteSheet.getFramePosition(frame + turretData.headings))
			}
		}
	}
}
