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

package nz.net.ultraq.redhorizon.graphics

import org.joml.Vector2f

/**
 * A material is a set of properties applied to the shape being rendered, often
 * being used to configure a shader.
 *
 * This particular base material only defines a texture, with subclasses being
 * used to add other properties.
 *
 * @author Emanuel Rabina
 */
class Material {

	Texture texture
	Vector2f frameXY
}
