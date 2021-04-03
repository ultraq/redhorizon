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

package nz.net.ultraq.redhorizon.assets

import nz.net.ultraq.redhorizon.engine.graphics.Texture

/**
 * A combination of several images, all laid out within a single texture so that
 * only 1 texture unit is required for all of the image data.
 * 
 * @author Emanuel Rabina
 */
class TextureAtlas {

	// Fix to holding 24x24 tiles (map tiles) for now
	final int width = 2400
	final int height = 2400

	private final List<Texture> textures = []

	
}
