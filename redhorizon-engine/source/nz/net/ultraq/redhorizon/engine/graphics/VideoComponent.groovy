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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.audio.AudioComponent
import nz.net.ultraq.redhorizon.engine.time.TimeComponent
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Video
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.scenegraph.LocalTransform

import org.joml.Matrix4f

import groovy.transform.TupleConstructor

/**
 * A component for adding a video to an entity.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class VideoComponent implements GraphicsComponent<VideoComponent, SceneShaderContext>, AudioComponent<VideoComponent>,
	TimeComponent<VideoComponent>, LocalTransform<VideoComponent> {

	@Delegate(interfaces = false, includes = ['isPlaying', 'isStopped', 'play', 'stop'])
	final Video video
	private final Matrix4f globalTransformResult = new Matrix4f()

	@Override
	Class<? extends Shader> getShaderClass() {

		return BasicShader
	}

	@Override
	void render() {

		video.render(entity.globalPosition)
	}

	@Override
	void render(SceneShaderContext shaderContext) {

		video.render(shaderContext, entity.globalTransform.mul(transform, globalTransformResult))
	}

	@Override
	void update(float delta) {

		video.update(delta)
	}
}
