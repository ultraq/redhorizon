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
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.events.EventListener
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Vector3f
import static org.lwjgl.glfw.GLFW.*

/**
 * Load a collection of images into existing engines.
 * 
 * @author Emanuel Rabina
 */
class ImagesLoader extends MediaLoader<ImagesFile, ImageStrip> implements EventListener<KeyEvent> {

	private final Palette palette
	private final GraphicsEngine graphicsEngine
	private final InputEventStream inputEventStream
	private final Vector3f center
	private final int tick

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

		center = new Vector3f()
		tick = imagesFile.width
	}

	@Override
	void handleEvent(KeyEvent event) {

		if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
			switch (event.key) {
				case GLFW_KEY_LEFT:
					graphicsEngine.camera.translate(tick, 0)
					break
				case GLFW_KEY_RIGHT:
					graphicsEngine.camera.translate(-tick, 0)
					break
				case GLFW_KEY_SPACE:
					graphicsEngine.camera.center(center)
					break
			}
		}
	}

	@Override
	ImageStrip load() {

		media = new ImageStrip(file, palette)
		scene << media
		inputEventStream.on(KeyEvent, this)

		return media
	}

	@Override
	void unload() {

		inputEventStream.off(KeyEvent, this)
		scene.removeSceneElement(media)
	}
}
