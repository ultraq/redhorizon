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

import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.graphics.Colour

/**
 * A script to make the background colour black and disable the grid lines.
 *
 * @author Emanuel Rabina
 */
class DarkPreviewScript extends EntityScript implements AutoCloseable {

	private ExplorerScene scene

	@Override
	void close() {

		scene.window.withBackgroundColour(Colour.GREY)
		scene.gridLines.enable()
	}

	@Override
	void init() {

		scene = entity.scene as ExplorerScene
		scene.window.withBackgroundColour(Colour.BLACK)
		scene.gridLines.disable()
	}
}
