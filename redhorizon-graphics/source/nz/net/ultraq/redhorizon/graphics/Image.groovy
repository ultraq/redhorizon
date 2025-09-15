/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.ImageDecoder.FrameDecodedEvent
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

/**
 * A simple texture and rectangle mesh pair for displaying a 2D image in a
 * scene.
 *
 * @author Emanuel Rabina
 */
class Image implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Image)

	@Lazy
	private static volatile Mesh imageMesh = { createImageMesh() }()
	private static AtomicInteger instances = new AtomicInteger()

	private final Mesh mesh
	private final Texture texture
	final Material material
	final Matrix4f transform

	/**
	 * Constructor, create a new image using its name and a stream of data.
	 */
	Image(String fileName, InputStream inputStream) {

		ByteBuffer imageData = null
		Palette palette = null
		var result = ImageDecoders
			.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))
			.on(FrameDecodedEvent) { event ->
				imageData = event.data()
				palette = event.palette()
			}
			.decode(inputStream)
		while (!imageData) {
			Thread.onSpinWait()
		}

		var fileInformation = result.fileInformation()
		if (fileInformation) {
			logger.info('{}: {}', fileName, fileInformation)
		}

		mesh = imageMesh
		instances.incrementAndGet()
		texture = palette ?
			new OpenGLTexture(result.width(), result.height(), palette.channels, imageData.applyPalette(palette)) :
			new OpenGLTexture(result.width(), result.height(), result.channels(), imageData)
		material = new Material(texture: texture)
		transform = new Matrix4f().scale(result.width(), result.height(), 1)
	}

	@Override
	void close() {

		texture?.close()
		mesh?.close()
		if (instances.decrementAndGet() == 0) {
			imageMesh?.close()
		}
	}

	/**
	 * Create the single mesh used for all image instances.  It's the image/model
	 * transform that will size and position the mesh to match what the image
	 * needs on rendering.
	 */
	private static Mesh createImageMesh() {

		return new OpenGLMesh(Type.TRIANGLES,
			new Vertex[]{
				new Vertex(new Vector3f(0, 0, 0), Colour.WHITE, new Vector2f(0, 0)),
				new Vertex(new Vector3f(1, 0, 0), Colour.WHITE, new Vector2f(1, 0)),
				new Vertex(new Vector3f(1, 1, 0), Colour.WHITE, new Vector2f(1, 1)),
				new Vertex(new Vector3f(0, 1, 0), Colour.WHITE, new Vector2f(0, 1))
			},
			new int[]{ 0, 1, 2, 2, 3, 0 }
		)
	}

	/**
	 * Draw this image, using the current shader.
	 */
	void draw(ShaderContext shaderContext) {

		shaderContext.applyUniforms(transform, material, null)
		mesh.draw()
	}
}
