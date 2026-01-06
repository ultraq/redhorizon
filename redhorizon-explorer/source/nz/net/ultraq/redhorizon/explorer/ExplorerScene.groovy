/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.GridLinesEntity
import nz.net.ultraq.redhorizon.explorer.mixdata.MixDatabase
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.objects.GlobalPalette
import nz.net.ultraq.redhorizon.explorer.objects.ImagePreview
import nz.net.ultraq.redhorizon.explorer.objects.PalettePreview
import nz.net.ultraq.redhorizon.explorer.objects.UiController
import nz.net.ultraq.redhorizon.explorer.ui.EntrySelectedEvent
import nz.net.ultraq.redhorizon.explorer.ui.TouchpadInputEvent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Explorer UI and preview area.
 *
 * @author Emanuel Rabina
 */
class ExplorerScene extends Scene implements EventTarget<ExplorerScene> {

	private static final Logger logger = LoggerFactory.getLogger(ExplorerScene)
	private static final Colour GRID_LINES_GREY = new Colour('GridLines-Grey', 0.6f, 0.6f, 0.6f)
	private static final Colour GRID_LINES_DARK_GREY = new Colour('GridLines-DarkGrey', 0.2f, 0.2f, 0.2f)

//	private final ResourceManager resourceManager = new ResourceManager(
//		new File(System.getProperty('user.dir'), 'mix'),
//		'nz.net.ultraq.redhorizon.filetypes',
//		'nz.net.ultraq.redhorizon.classic.filetypes')
	private final MixDatabase mixDatabase = new MixDatabase()

	final CameraEntity camera
	final GridLinesEntity gridLines
	private final Window window
	private Entity preview
	private InputStream selectedFileInputStream

	/**
	 * Constructor, create the initial scene (blank, unless asked to load a file
	 * at startup).
	 */
	ExplorerScene(int width, int height, Window window, boolean touchpadInput, File startingDirectory) {

		this.window = window

		camera = new CameraEntity(width, height, window)
		addChild(camera)

		var uiController = new UiController(this, window, touchpadInput, startingDirectory)
		addChild(uiController.withName('UI'))

		// Main menu events
		uiController
			.on(TouchpadInputEvent) { event ->
//				var mapNode = this.findDescendent { it instanceof nz.net.ultraq.redhorizon.classic.maps.Map } as nz.net.ultraq.redhorizon.classic.maps.Map
//				if (mapNode) {
//					((MapViewerScript)mapNode.script).touchpadInput = event.touchpadInput()
//				}
			}

		// Entry list events
		uiController
			.on(EntrySelectedEvent) { event ->
				var entry = event.entry()
				if (entry instanceof MixEntry) {
					queueChange { ->
						clearPreview()
						preview(entry)
					}
				}
				else if (entry instanceof FileEntry) {
					if (entry.file.file) {
						queueChange { ->
							clearPreview()
							preview(entry.file)
						}
					}
					else {
						trigger(event)
					}
				}
			}

		gridLines = new GridLinesEntity(nz.net.ultraq.redhorizon.classic.maps.Map.MAX_BOUNDS, 24, GRID_LINES_DARK_GREY, GRID_LINES_GREY)
		addChild(gridLines)

		addChild(new GlobalPalette()
			.withName('Global palette'))
	}

	/**
	 * Clear the current entry in preview and reset the preview scene.
	 */
	private void clearPreview() {

		selectedFileInputStream?.close()
		if (preview) {
			removeChild(preview)
			preview.close()
			preview = null
		}
		camera.resetTransform()
	}

	@Override
	void close() {

		clearPreview()
		super.close()
	}

	/**
	 * Update the preview area with the media for the selected file.
	 */
	void preview(File file) {

		logger.info('Loading {}...', file.name)

		var fileClass = file.supportedFileClass
		if (fileClass) {
			selectedFileInputStream = file.newInputStream()
			var fileInstance = time("Reading file ${file.name} from filesystem", logger) { ->
				return fileClass.newInstance(file.name, selectedFileInputStream)
			}
			preview(fileInstance, file.name.substring(0, file.name.lastIndexOf('.')))
		}
		else {
			logger.info('No filetype implementation for {}', file.name)
		}
	}

	/**
	 * Update the preview area with the media for the selected mix file entry.
	 */
	void preview(MixEntry entry) {

		logger.info('Loading {} from mix file', entry.name ?: '(unknown)')

		var fileClass = entry.fileClass
		var fileName = entry.name
		var entryId = !fileName.contains('unknown') ? fileName.substring(0, fileName.indexOf('.')) : '(unknown)'

		if (fileClass) {
			selectedFileInputStream = new BufferedInputStream(entry.mixFile.getEntryData(entry.mixEntry))
			var fileInstance = time("Reading file ${fileName} from Mix file", logger) { ->
				return fileClass.newInstance(selectedFileInputStream)
			}
			preview(fileInstance, entryId)
		}
		else {
			logger.info('No filetype implementation for {}', entry.name ?: '(unknown)')
		}
	}

	/**
	 * Update the preview area for the given file data and type.
	 */
	void preview(Object file, String objectId) {

		var mediaNode = switch (file) {

		// Objects
//			case ShpFile ->
//				preview(file, objectId)
//			case TmpFileRA ->
//				preview(file, objectId)
//			case IniFile ->
//				preview(file as MapFile, objectId)
//
		// Media
			case Image ->
				new ImagePreview(window, this, file)
					.withName('Image - ${objectId}')
//			case VideoFile ->
//				new FullScreenContainer().addChild(new Video(file).attachScript(new PlaybackScript(true)))
//			case AnimationFile ->
//				new FullScreenContainer().addChild(new Animation(file).attachScript(new PlaybackScript(true)))
//			case SoundFile ->
//				new Sound(file).attachScript(new PlaybackScript(file.forStreaming))

				// 🤷
			case Palette ->
				new PalettePreview(this, file)
			default ->
				logger.info('Filetype of {} not yet configured', file.class.simpleName)
		}

		if (mediaNode) {
			addChild(mediaNode)
			preview = mediaNode
		}
	}

	/**
	 * Load up any unspecified multi-image file as a sprite to flip through its
	 * frames.
	 */
//	private void preview(ImagesFile imagesFile, String objectId) {
//
//		var sprite = new PalettedSprite(imagesFile).attachScript(new SpriteShowcaseScript(camera))
//		sprite.bounds { ->
//			center()
//		}
//		sprite.name = "PalettedSprite - ${objectId}"
//		scene << sprite
//		preview = sprite
//	}

	/**
	 * Attempt to load up an object from its corresponding SHP file.
	 */
//	private void preview(ShpFile shpFile, String objectId) {
//
//		String unitConfig
//		try {
//			unitConfig = getUnitDataJson(objectId)
//			logger.info('Configuration data:\n{}', JsonOutput.prettyPrint(unitConfig))
//		}
//		catch (IllegalArgumentException ignored) {
//			logger.info('No configuration available for {}', objectId)
//		}
//
//		// Found a unit config, use it to view the file
//		if (unitConfig) {
//			var unitData = new JsonSlurper().parseText(unitConfig) as UnitData
//			var targetClass = switch (unitData.type) {
//				case 'infantry', 'structure', 'vehicle', 'aircraft' -> Unit
//				default -> logger.info('Unit type {} not supported', unitData.type)
//			}
//			if (targetClass) {
//				var unit = targetClass
//					.getDeclaredConstructor(ImagesFile, UnitData)
//					.newInstance(shpFile, unitData)
//					.attachScript(new UnitShowcaseScript(camera))
//				unit.body.bounds { ->
//					center()
//				}
//				if (unit.turret) {
//					unit.turret.bounds { ->
//						center()
//					}
//				}
//				scene << unit
//				preview = unit
//			}
//		}
//
//		// No config found, fall back to viewing a SHP file as media
//		else {
//			preview(shpFile as ImagesFile, objectId)
//		}
//	}

	/**
	 * Load up a tile file and arrange it so that it looks complete.
	 */
//	private void preview(TmpFileRA tileFile, String objectId) {
//
//		var singleImageData = tileFile.imagesData.combine(tileFile.width, tileFile.height, tileFile.format, tileFile.tilesX)
//		var singleImageWidth = tileFile.tilesX * tileFile.width
//		var singleImageHeight = tileFile.tilesY * tileFile.height
//
//		var tile = new PalettedSprite(singleImageWidth, singleImageHeight, 1, 1f, 1f, { scene ->
//			return scene.requestCreateOrGet(new SpriteSheetRequest(singleImageWidth, singleImageHeight, tileFile.format, singleImageData))
//		})
//			.attachScript(new SpriteShowcaseScript(camera))
//		tile.bounds.center()
//		tile.name = "PalettedSprite - ${objectId}"
//		scene << tile
//		preview = tile
//	}

	/**
	 * Attempt to load up a map from its map file.
	 */
//	private void preview(MapFile mapFile, String objectId) {
//
//		var mapViewerScript = new MapViewerScript(camera, nodeList, options.touchpadInput)
//		time("Loading map ${objectId}", logger) { ->
//			resourceManager.withDirectory(currentDirectory) { ->
//				var map = new Map(mapFile, resourceManager).attachScript(mapViewerScript)
//				scene << map
//				preview = map
//			}
//		}
//		mapViewerScript.viewInitialPosition()
//	}
}
