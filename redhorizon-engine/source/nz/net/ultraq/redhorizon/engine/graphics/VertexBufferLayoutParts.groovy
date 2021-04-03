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
@TupleConstructor(defaults = false)
enum VertexBufferLayoutParts {

	COLOUR      ('colour',     0, Colour.FLOATS),
	POSITION    ('position',   1, Vector2f.FLOATS),
	TEXCOORD    ('texCoord',   2, Vector2f.FLOATS),
	TEXUNIT     ('texUnit',    3, 1),
	TEXLAYER    ('texLayer',   4, 1),
	MODEL_INDEX ('modelIndex', 5, 1)

	final String name
	final int location
	final int size
}
