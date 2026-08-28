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

import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Sprite

import static org.lwjgl.glfw.GLFW.*

/**
 * A general-purpose script for scrolling through the available frames for a
 * sprite sheet.
 *
 * @author Emanuel Rabina
 */
class SpritePreviewScript extends Script {

	private Sprite sprite
	private int frame = 0
	private float timer
	private float frameInterval = 0.1f
	private boolean autoplay = true

	@Override
	void init() {

		node.scene.find(Camera).scale(2f)
		sprite = node.find(Sprite)
	}

	@Override
	void update(float delta) {

		timer += delta

		if ((input.keyPressed(GLFW_KEY_A) || input.keyPressed(GLFW_KEY_LEFT)) && timer >= frameInterval) {
			frame = Math.max(frame - 1, 0)
			timer = 0f
			autoplay = false
		}
		else if ((input.keyPressed(GLFW_KEY_D) || input.keyPressed(GLFW_KEY_RIGHT)) && timer >= frameInterval) {
			frame = Math.min(frame + 1, sprite.spriteSheet.numFrames - 1)
			timer = 0f
			autoplay = false
		}
		else if (autoplay && timer >= frameInterval) {
			frame = (frame + 1) % sprite.spriteSheet.numFrames
			timer = 0f
		}

		sprite.withFramePosition(frame)
	}
}
