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

package nz.net.ultraq.redhorizon.cli.mediaplayer

import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.engine.scripting.Script

/**
 * A script to make a sprite node behave as a full-screen image.
 *
 * @author Emanuel Rabina
 */
class ImageScript extends Script<Sprite> {

	@Delegate
	private Sprite applyDelegate() {
		return scriptable
	}

	@Override
	void onSceneAdded(Scene scene) {

		var width = imageFile.width
		var height = imageFile.height
		scaleXY(scene.window.renderResolution.calculateScaleToFit(width, height))
		translate(-width / 2, -height / 2)
	}
}
