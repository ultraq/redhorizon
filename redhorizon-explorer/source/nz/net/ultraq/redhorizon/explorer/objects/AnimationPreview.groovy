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

package nz.net.ultraq.redhorizon.explorer.objects

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.AnimationComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window

/**
 * For viewing an animation file.
 *
 * @author Emanuel Rabina
 */
class AnimationPreview extends Entity<AnimationPreview> {

	private final Window window

	/**
	 * Constructor, create a preview with a sprite component and to change the
	 * background to black.
	 */
	AnimationPreview(Window window, Animation animation, String objectId) {

		this.window = window.withBackgroundColour(Colour.BLACK)

		// TODO: Is there some better way to convey animations that needs special scaling?
		var animationComponent = new AnimationComponent(animation)
		if (objectId.endsWith('.wsa')) {
			animationComponent.scale(2f, 2.4f)
		}
		addComponent(animationComponent)
		addComponent(new ScriptComponent(AnimationPreviewScript))
	}

	static class AnimationPreviewScript extends EntityScript<AnimationPreview> implements AutoCloseable {

		private ExplorerScene scene
		private AnimationComponent animationComponent
		private boolean playbackStarted = false

		@Override
		void close() {

			entity.window.withBackgroundColour(Colour.GREY)
			scene.gridLines.enable()
		}

		@Override
		void init() {

			scene = entity.scene as ExplorerScene
			animationComponent = entity.findComponentByType(AnimationComponent)

			entity.window.withBackgroundColour(Colour.BLACK)
			scene.gridLines.disable()
		}

		@Override
		void update(float delta) {

			if (!playbackStarted && !animationComponent.animation.playing) {
				animationComponent.animation.play()
				playbackStarted = true
			}

			if (playbackStarted && animationComponent.animation.stopped) {
				scene.queueChange { ->
					scene.removeChild(entity)
					entity.close()
				}
			}
		}
	}
}
