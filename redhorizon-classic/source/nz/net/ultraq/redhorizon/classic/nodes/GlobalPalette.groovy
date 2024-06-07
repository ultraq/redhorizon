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

package nz.net.ultraq.redhorizon.classic.nodes

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.UniformBufferRequest
import nz.net.ultraq.redhorizon.engine.graphics.UniformBuffer
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.FrustumIntersection

import groovy.transform.TupleConstructor
import java.nio.FloatBuffer
import java.util.concurrent.CompletableFuture

/**
 * The scene-wide C&C palette and alpha mask.  These are represented as global
 * uniform buffer objects so are present in every use of the paletted sprite
 * shader for this package, ie: every {@link PalettedSprite} instance.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class GlobalPalette extends Node<GlobalPalette> implements GraphicsElement {

	Palette palette

	private UniformBuffer paletteAndAlphaMaskBuffer
	private boolean paletteChanged

	/**
	 * Create a {@code FloatBuffer} that can be used as data to the palette
	 * uniform buffer.
	 */
	private FloatBuffer buildPaletteBuffer() {

		return (0..255)
			.inject(FloatBuffer.allocate(4 * 256)) { buffer, i ->
				var colour = palette[i]
				return buffer.put(new float[]{ (colour[0] & 0xff) / 256, (colour[1] & 0xff) / 256, (colour[2] & 0xff) / 256, 1 })
			}
			.flip()
	}

	@Override
	boolean isVisible(FrustumIntersection frustumIntersection) {

		// Always visible so that palette updates can be applied
		return true
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return CompletableFuture.supplyAsync { ->
			var paletteData = buildPaletteBuffer()
			var alphaMaskData = (0..255)
				.inject(FloatBuffer.allocate(4 * 256)) { buffer, i ->
					return switch (i) {
						case 0 -> buffer.put(new float[]{ 0, 0, 0, 0 })
						case 4 -> buffer.put(new float[]{ 0, 0, 0, 0.5 })
						default -> buffer.put(new float[]{ 1, 1, 1, 1 })
					}
				}
				.flip()
			return FloatBuffer.allocate(8 * 256)
				.put(paletteData)
				.put(alphaMaskData)
				.flip()
		}
			.thenComposeAsync { paletteAndAlphaMaskData ->
				return scene.requestCreateOrGet(new UniformBufferRequest('Palette', paletteAndAlphaMaskData))
			}
			.thenAcceptAsync { newUniformBuffer ->
				paletteAndAlphaMaskBuffer = newUniformBuffer
			}
	}

	@Override
	void render(GraphicsRenderer renderer) {
	}

	/**
	 * Update the palette being used.
	 */
	void setPalette(Palette palette) {

		this.palette = palette
		paletteChanged = true
	}

	@Override
	void update() {

		if (paletteChanged) {
			paletteAndAlphaMaskBuffer.updateBufferData(buildPaletteBuffer())
			paletteChanged = false
		}
	}
}
