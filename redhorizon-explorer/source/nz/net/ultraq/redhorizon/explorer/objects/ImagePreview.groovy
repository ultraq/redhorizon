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
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader

/**
 * For viewing an image file.
 *
 * @author Emanuel Rabina
 */
class ImagePreview extends Entity<ImagePreview> {

	private final Window window

	/**
	 * Constructor, create a preview with a sprite component and to change the
	 * background to black.
	 */
	ImagePreview(Window window, Image image) {

		this.window = window.withBackgroundColour(Colour.BLACK)

		addComponent(new SpriteComponent(image, BasicShader))
		addComponent(new ScriptComponent(ImagePreviewScript))
	}

	static class ImagePreviewScript extends EntityScript<ImagePreview> implements AutoCloseable {

		private ExplorerScene scene

		@Override
		void close() {

			entity.window.withBackgroundColour(Colour.GREY)
			scene.gridLines.enable()
		}

		@Override
		void init() {

			scene = entity.scene as ExplorerScene
			entity.window.withBackgroundColour(Colour.BLACK)
			scene.gridLines.disable()
		}
	}
}
