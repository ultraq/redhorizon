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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.Mesh

import org.joml.Vector2f

import groovy.transform.MapConstructor

/**
 * OpenGL implementation of a mesh.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor
class OpenGLMesh extends Mesh {

	final int vertexType
	final Colour colour
	final Vector2f[] vertices
	final Vector2f[] textureUVs
	int vertexArrayId
	int vertexBufferId

	final int[] indices
	int elementBufferId
}
