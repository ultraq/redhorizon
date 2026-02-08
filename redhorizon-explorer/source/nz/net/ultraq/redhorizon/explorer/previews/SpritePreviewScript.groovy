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

import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.explorer.ExplorerScene

import static org.lwjgl.glfw.GLFW.*

/**
 * A general-purpose script for scrolling through the available frames for a
 * sprite sheet.
 *
 * @author Emanuel Rabina
 */
class SpritePreviewScript extends Script {

	private SpriteComponent sprite
	private int frame = 0
	private float repeatTimer
	private float repeatInterval = 0.1f

	@Override
	void init() {

		(node.scene as ExplorerScene).camera.scale(2f)
		sprite = node.findComponentByType(SpriteComponent)
	}

	@Override
	void update(float delta) {

		repeatTimer += delta
		if ((input.keyPressed(GLFW_KEY_A) || input.keyPressed(GLFW_KEY_LEFT)) && repeatTimer >= repeatInterval) {
			frame = Math.max(frame - 1, 0)
			repeatTimer = 0f
		}
		else if ((input.keyPressed(GLFW_KEY_D) || input.keyPressed(GLFW_KEY_RIGHT)) && repeatTimer >= repeatInterval) {
			frame = Math.min(frame + 1, sprite.spriteSheet.numFrames - 1)
			repeatTimer = 0f
		}

		sprite.framePosition.set(sprite.spriteSheet.getFramePosition(frame))
	}
}
