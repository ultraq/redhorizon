/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.scenegraph.Scene

import groovy.transform.TupleConstructor

/**
 * Load a collection of images into existing engines.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ImagesLoader implements MediaLoader<ImagesFile, ImageStrip> {

	final Palette palette
	final Scene scene

	@Override
	ImageStrip load(ImagesFile imagesFile) {

		def imageStrip = new ImageStrip(imagesFile, palette)
		scene << imageStrip
		return imageStrip
	}
}
