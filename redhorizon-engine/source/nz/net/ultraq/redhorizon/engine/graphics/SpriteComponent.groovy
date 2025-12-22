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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * A component for adding a {@link Sprite} to an entity.
 *
 * @author Emanuel Rabina
 */
class SpriteComponent extends GraphicsComponent<SpriteComponent, SceneShaderContext> implements AutoCloseable {

	final Sprite sprite
	final Vector2f framePosition = new Vector2f()
	final Matrix4f transform = new Matrix4f()
	final Image image
	final SpriteSheet spriteSheet
	final Class<? extends Shader> shaderClass
	final int width
	final int height
	private final Matrix4f globalTransform = new Matrix4f()
	private final Vector3f _position = new Vector3f()

	/**
	 * Constructor, use the given image for the sprite.
	 */
	SpriteComponent(Image image, Class<? extends Shader> shaderClass) {

		this.image = image
		this.spriteSheet = null
		this.shaderClass = shaderClass
		this.width = image.width
		this.height = image.height

		sprite = new Sprite(image)
	}

	/**
	 * Constructor, use the given sprite sheet for the sprite.
	 */
	SpriteComponent(SpriteSheet spriteSheet, Class<? extends Shader> shaderClass) {

		this.image = null
		this.spriteSheet = spriteSheet
		this.shaderClass = shaderClass
		this.width = spriteSheet.frameWidth
		this.height = spriteSheet.frameHeight

		sprite = new Sprite(spriteSheet)
	}

	@Override
	void close() {

		sprite.close()
	}

	/**
	 * Return the local position of the sprite component.
	 */
	Vector3fc getPosition() {

		return transform.getTranslation(_position)
	}

	/**
	 * Render the sprite.
	 */
	@Override
	void render(SceneShaderContext shaderContext) {

		sprite.render(shaderContext, globalTransform.set(parent.transform).mul(transform), framePosition)
	}

	/**
	 * Update the local position of the sprite component.
	 */
	void setPosition(float x, float y, float z) {

		transform.setTranslation(x, y, z)
	}

	/**
	 * Modify the transform of this component.
	 */
	SpriteComponent translate(float x, float y, float z) {

		transform.translate(x, y, z)
		return this
	}
}
