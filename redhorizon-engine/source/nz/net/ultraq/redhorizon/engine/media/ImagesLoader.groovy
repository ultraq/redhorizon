/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.media

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Vector3f
import static org.lwjgl.glfw.GLFW.*

/**
 * Load a collection of images into existing engines.
 *
 * @author Emanuel Rabina
 */
class ImagesLoader extends MediaLoader<ImagesFile, ImageStrip> {

	private final Palette palette
	private final GraphicsEngine graphicsEngine
	private final InputEventStream inputEventStream
	private List<RemoveControlFunction> removeControlFunctions = []

	/**
	 * Constructor, create a loader for an images file.
	 *
	 * @param imagesFile
	 * @param palette
	 * @param scene
	 * @param graphicsEngine
	 * @param inputEventStream
	 */
	ImagesLoader(ImagesFile imagesFile, Palette palette, Scene scene, GraphicsEngine graphicsEngine,
		InputEventStream inputEventStream) {

		super(imagesFile, scene)
		this.palette = palette
		this.graphicsEngine = graphicsEngine
		this.inputEventStream = inputEventStream
	}

	@Override
	ImageStrip load() {

		media = new ImageStrip(file, palette)
		scene << media

		var center = new Vector3f()
		var tick = file.width

		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_LEFT, 'Scroll left', { ->
			graphicsEngine.camera.translate(tick, 0)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_RIGHT, 'Scroll right', { ->
			graphicsEngine.camera.translate(-tick, 0)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Reset scroll', { ->
			graphicsEngine.camera.center(center)
		}))

		return media
	}

	@Override
	void unload() {

		removeControlFunctions*.apply(null)
		scene.removeSceneElement(media)
	}
}
