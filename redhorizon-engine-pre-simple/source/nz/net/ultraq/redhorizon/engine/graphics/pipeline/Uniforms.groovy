/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics.pipeline

import nz.net.ultraq.redhorizon.engine.graphics.Uniform
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.graphics.Material
import nz.net.ultraq.redhorizon.graphics.Shader

import groovy.transform.PackageScope

/**
 * A collection of common shader uniforms for each of the render pipeline
 * rendering passes.
 *
 * @author Emanuel Rabina
 */
@PackageScope
class Uniforms {

	static final Uniform framebufferUniform = { Shader shader, Material material, Window window ->
		shader.setUniform('framebuffer', 0, material.texture)
	}

	static final Uniform textureTargetSizeUniform = { Shader shader, Material material, Window window ->
		shader.setUniform('textureTargetSize', window.targetResolution as float[])
	}
}
