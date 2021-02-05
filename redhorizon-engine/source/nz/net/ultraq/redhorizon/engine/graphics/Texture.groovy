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

import groovy.transform.MapConstructor
import groovy.transform.PackageScope
import groovy.transform.VisibilityOptions
import static groovy.transform.options.Visibility.PACKAGE_PRIVATE

/**
 * Representation of a single texture to render.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor(visibilityId = 'default')
@VisibilityOptions(id = 'default', constructor = PACKAGE_PRIVATE)
class Texture {

	final int textureId

	@PackageScope
	final List<MappedTexture> instances = []
}
