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

import org.joml.Vector2f

import groovy.transform.TupleConstructor

/**
 * A description of the data comprising a section of a vertex buffer layout.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor
enum VertexBufferLayoutPart {

	// NOTE: The location values should line up with what's in the shaders as that
	//       is what's needed for the index param in glEnableVertexAttribArray and
	//       glVertexAttribPointer.  If we want to stop having to keep these all
	//       in sync, then alternatives would be to:
	//        - assign these ourselves when shaders are created, after
	//          glAttachShader and before glLinkProgram, using glBindAttribLocation
	//        - use a query to look these up using glGetAttribLocation, only doing
	//          this once per vertex buffer otherwise it can crash on Windows
	// @formatter:off
	POSITION    ('position',   0, Vector2f.FLOATS),
	COLOUR      ('colour',     1, Colour.FLOATS),
	TEXTURE_UVS ('textureUVs', 2, Vector2f.FLOATS)
	// @formatter:on

	final String name
	final int location
	final int size
}
