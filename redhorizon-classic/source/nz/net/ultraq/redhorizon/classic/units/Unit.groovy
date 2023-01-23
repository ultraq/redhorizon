/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.classic.shaders.ShaderConfigs
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneElement
import nz.net.ultraq.redhorizon.engine.time.Temporal
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

/**
 * The unit that gets displayed on the screen.
 *
 * @author Emanuel Rabina
 */
abstract class Unit implements GraphicsElement, SceneElement<Unit>, Rotatable, Temporal {

	private static Logger logger = LoggerFactory.getLogger(Unit)

	final int width
	final int height
	final ImagesFile imagesFile
	final Palette palette
	final List<UnitPart> parts = []
	final List<UnitState> states = []

	Faction faction = Faction.GOLD

	protected Shader shader
	protected Texture texture
	protected int spritesHorizontal
	protected int spritesVertical
	protected int spriteSheetWidth
	protected int spriteSheetHeight
	protected float frameStepX
	protected float frameStepY
	protected Material material
	// TODO: State machine for transitioning between states?
	protected int stateIndex
	protected long animationStartTime

	/**
	 * Constructor, create a new basic unit.
	 *
	 * @param imagesFile
	 * @param palette
	 */
	protected Unit(ImagesFile imagesFile, Palette palette) {

		this.width = imagesFile.width
		this.height = imagesFile.height
		bounds.set(0, 0, width, height)

		this.imagesFile = imagesFile
		this.palette = palette
	}

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	/**
	 * A shortcut to retrieving the {@code UnitBody} part of a unit.
	 *
	 * @return
	 */
	UnitBody getBody() {

		return parts.find { it instanceof UnitBody } as UnitBody
	}

	@Override
	void init(GraphicsRenderer renderer) {

		shader = renderer.createShader(ShaderConfigs.PALETTED_SHADER)

		// TODO: Load the palette once
		var paletteAsTexture = renderer.createTexture(256, 1, palette.format, palette as ByteBuffer)

		spritesHorizontal = Math.min(imagesFile.numImages, renderer.maxTextureSize / imagesFile.width as int)
		spritesVertical = Math.ceil(imagesFile.numImages / spritesHorizontal) as int
		spriteSheetWidth = spritesHorizontal * imagesFile.width
		spriteSheetHeight = spritesVertical * imagesFile.height
		logger.debug('Sprite sheet - across: {}, down: {}, width: {}, height: {}',
			spritesHorizontal, spritesVertical, spriteSheetWidth, spriteSheetHeight)

		frameStepX = 1 / spritesHorizontal
		frameStepY = 1 / spritesVertical
		logger.debug('Texture UV steps for sprite sheet: {}x{}', frameStepX, frameStepY)

		var imagesAsSpriteSheet = imagesFile.imagesData
			.flipVertical(imagesFile.width, imagesFile.height, imagesFile.format)
			.combineImages(imagesFile.width, imagesFile.height, spritesHorizontal)
		texture = renderer.createTexture(spriteSheetWidth, spriteSheetHeight, imagesFile.format, imagesAsSpriteSheet)
		material = renderer.createMaterial(texture, transform)
		material.palette = paletteAsTexture
		material.faction = faction

		parts*.init(renderer)
		states*.init(renderer)
	}

	/**
	 * Selects this unit's next state for rendering.
	 */
	void nextState() {

		setState(+1)
	}

	/**
	 * Selects this unit's previous animation for rendering.
	 */
	void previousState() {

		setState(-1)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		parts*.render(renderer)
	}

	/**
	 * Select the next animation either next or previously in the sequence.
	 *
	 * @param next
	 */
	private void setState(int next) {

		if (states.size()) {
			stateIndex = Math.wrap(stateIndex + next, 0, states.size())
			logger.debug("${stateIndex == -1 ? 'body' : states[stateIndex].name} state selected")
		}
	}

	@Override
	void setHeading(float newHeading) {

		Rotatable.super.setHeading(newHeading)
		parts.each { part ->
			if (part instanceof Rotatable) {
				part.heading = newHeading
			}
		}
	}

	/**
	 * (Re)start playing the current animation.
	 */
	void startAnimation() {

		animationStartTime = currentTimeMs
	}
}
