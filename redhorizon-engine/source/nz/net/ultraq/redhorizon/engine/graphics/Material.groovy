/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.joml.Matrix4f

import groovy.transform.MapConstructor

/**
 * A material defines how a shape should be rendered.  A {@link Mesh} already
 * covers the shape part, so a material covers the uniform values that go into
 * configuring a shader.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor
class Material {

	Mesh mesh
	Texture texture
	Shader shader
	Matrix4f transform
}
