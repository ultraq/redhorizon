/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGBA

import org.joml.Rectanglef

import java.nio.ByteBuffer

/**
 * A scanline overlay to use with a low-resolution image of some kind.
 * 
 * @author Emanuel Rabina
 */
class Scanlines implements GraphicsElement, SceneElement<SceneElement> {

	private final Dimension overlay
	private Material material

	/**
	 * Constructor, set the size of the object over which the scanlines are to
	 * show.
	 * 
	 * @param overlay
	 */
	Scanlines(Dimension overlay) {

		this.overlay = new Dimension(overlay.width, overlay.height * 2 + 1)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		// Build a texture to look like scanlines
		def scanlineTexture = ByteBuffer.allocateNative(overlay.width * overlay.height * 4)
		for (def y = 0; y < overlay.height; y += 2) {
			scanlineTexture.position(y * overlay.width * 4)
			for (def x = 0; x < overlay.width; x++) {
				scanlineTexture.advance(3).put((byte)0x40)
			}
		}
		scanlineTexture.rewind()

		material = renderer.createMaterial(
			renderer.createSpriteMesh(new Rectanglef(0, 0, overlay.width, overlay.height)),
			renderer.createTexture(scanlineTexture, FORMAT_RGBA.value, overlay.width, overlay.height, true)
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(material, transform)
	}
}
