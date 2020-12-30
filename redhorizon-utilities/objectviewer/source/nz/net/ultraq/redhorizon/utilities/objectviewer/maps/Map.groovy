/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.utilities.objectviewer.maps

import nz.net.ultraq.redhorizon.classic.filetypes.ini.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.pal.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.tmp.TmpFileRA
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

import org.joml.Rectanglef

/**
 * A map on which a mission or a skirmish can take place.
 * 
 * @author Emanuel Rabina
 */
class Map implements GraphicsElement, SelfVisitable {

	private final String name
	private final Theaters theater
	private final Image background

	/**
	 * Construtor, build a map from the given map file.
	 * 
	 * @param mapFile
	 */
	Map(IniFile mapFile) {

		name = mapFile['Basic']['Name']

		def theaterString = mapFile['Map']['Theater']
		theater = Theaters.find { theater ->
			return theater.label.equalsIgnoreCase(theaterString)
		}
		// TODO: Figure out some way to share knowledge of the path to mix files
		//       containing the necessary files
		def tilesetMixFile = new MixFile(new File("mix/red-alert/MapTiles_${theater.label}.mix"))
		def clearTileName = TilesRA.DEFAULT.name + theater.ext
		def backgroundTileFile = tilesetMixFile.getEntryData(tilesetMixFile.getEntry(clearTileName)).withStream { inputStream ->
			return new TmpFileRA(inputStream)
		}
		def palette = this.class.classLoader.getResourceAsStream("ra-${theater.label}.pal").withStream { inputStream ->
			return new PalFile(inputStream)
		}

		// Use the background tile to create a 5x4 repeating image
		def combinedBackgroundData = backgroundTileFile.imagesData
			.combineImages(backgroundTileFile.width, backgroundTileFile.height, 5)
			.applyPalette(palette)
		def combinedWidth = backgroundTileFile.width * 5
		def combinedHeight = backgroundTileFile.height * 4
		background = new Image(combinedWidth, combinedHeight, FORMAT_RGB.value, combinedBackgroundData,
			new Rectanglef(-combinedWidth * 100, -combinedHeight * 100, combinedWidth * 100, combinedHeight * 100),
			100, 100
		)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		background.delete(renderer)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		background.init(renderer)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		background.render(renderer)
	}

	/**
	 * Return some information about this map.
	 * 
	 * @return Map info.
	 */
	@Override
	String toString() {

		return "Name: ${name}, Theater: ${theater}"
	}
}
