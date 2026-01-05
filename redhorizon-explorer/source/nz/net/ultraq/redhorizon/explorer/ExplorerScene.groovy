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
import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.PalFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.RaMixDatabase
import nz.net.ultraq.redhorizon.classic.graphics.PaletteComponent
import nz.net.ultraq.redhorizon.classic.maps.Map
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.GridLinesEntity
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.explorer.mixdata.MixDatabase
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntryTester
import nz.net.ultraq.redhorizon.explorer.objects.Palette
import nz.net.ultraq.redhorizon.explorer.scripts.MapViewerScript
import nz.net.ultraq.redhorizon.explorer.ui.CyclePaletteEvent
import nz.net.ultraq.redhorizon.explorer.ui.EntryList
import nz.net.ultraq.redhorizon.explorer.ui.EntrySelectedEvent
import nz.net.ultraq.redhorizon.explorer.ui.ExitEvent
import nz.net.ultraq.redhorizon.explorer.ui.MainMenuBar
import nz.net.ultraq.redhorizon.explorer.ui.TouchpadInputEvent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.input.KeyBinding
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Explorer UI and preview area.
 *
 * @author Emanuel Rabina
 */
class ExplorerScene extends Scene implements EventTarget<ExplorerScene> {

	private static final Logger logger = LoggerFactory.getLogger(ExplorerScene)

	private final List<Entry> entries = new CopyOnWriteArrayList<>()
//	private final ResourceManager resourceManager = new ResourceManager(
//		new File(System.getProperty('user.dir'), 'mix'),
//		'nz.net.ultraq.redhorizon.filetypes',
//		'nz.net.ultraq.redhorizon.classic.filetypes')
	private final ResourceManager resourceManager
	private final MixDatabase mixDatabase = new MixDatabase()

	private CameraEntity camera
	private Entity globalPalette
	private Entity preview
	private File currentDirectory
	private InputStream selectedFileInputStream
	private PaletteType currentPalette

	/**
	 * Constructor, create the initial scene (blank, unless asked to load a file
	 * at startup).
	 */
	ExplorerScene(int width, int height, Window window, ResourceManager resourceManager, InputEventHandler input,
		boolean touchpadInput, File startingDirectory) {

		this.resourceManager = resourceManager

		camera = new CameraEntity(width, height, window)
		addChild(camera)

		var mainMenuBar = new MainMenuBar(touchpadInput)
		var entryList = new EntryList(entries)
		addChild(new Entity()
			.addComponent(new ImGuiComponent(new DebugOverlay()
				.withCursorTracking(camera, window)))
			.addComponent(new ImGuiComponent(mainMenuBar))
			.addComponent(new ImGuiComponent(new NodeList(this)))
			.addComponent(new ImGuiComponent(entryList))
			.addComponent(new ImGuiComponent(new LogPanel()))
			.withName('UI'))

		// Main menu events
		mainMenuBar
			.on(ExitEvent) { event ->
				window.shouldClose(true)
			}
			.on(TouchpadInputEvent) { event ->
				var mapNode = this.findDescendent { it instanceof Map } as Map
				if (mapNode) {
					((MapViewerScript)mapNode.script).touchpadInput = event.touchpadInput()
				}
			}
			.on(CyclePaletteEvent) { event ->
				cyclePalette()
			}

		// Entry list events
		entryList
			.on(EntrySelectedEvent) { event ->
				var entry = event.entry()
				if (entry instanceof MixEntry) {
					if (entry.name == '..') {
						buildList(currentDirectory)
					}
					else {
						clearPreview()
						preview(entry)
					}
				}
				else if (entry instanceof FileEntry) {
					var file = entry.file
					if (file.directory) {
						buildList(file)
					}
					else if (file.name.endsWith('.mix')) {
						buildList(new MixFile(file))
					}
					else {
						clearPreview()
						preview(file)
					}
				}
			}
			.relay(EntrySelectedEvent, this)

		addChild(new GridLinesEntity(Map.MAX_BOUNDS, 24, Colour.RED, Colour.YELLOW))

		globalPalette = new Entity()
			.addComponent(new PaletteComponent(loadPalette()))
			.withName('Palette')
		addChild(globalPalette)

		input.addInputBinding(new KeyBinding(GLFW_KEY_P, true, { ->
			cyclePalette()
		}))

		buildList(startingDirectory)
	}

	/**
	 * Update the contents of the list from the current directory.
	 */
	private void buildList(File directory) {

		entries.clear()

		if (directory.parent) {
			entries << new FileEntry(
				file: directory.parentFile,
				name: '/..'
			)
		}
		directory
			.listFiles()
			.sort { file1, file2 ->
				file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
						file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				entries << new FileEntry(
					file: fileOrDirectory,
//					type: fileOrDirectory.file ? fileOrDirectory.name.fileClass?.simpleName : null
					type: null
				)
			}

		currentDirectory = directory
	}

	/**
	 * Update the contents of the list from the current mix file.
	 */
	private void buildList(MixFile mixFile) {

		entries.clear()
		entries << new MixEntry(mixFile, null, '..')

		// RA-MIXer built-in database, if available
		var raMixDbEntry = mixFile.getEntry(0x7fffffff)
		var raMixDb = raMixDbEntry ? new RaMixDatabase(mixFile.getEntryData(raMixDbEntry)) : null

		// TODO: Also support XCC local database

		var mixEntryTester = new MixEntryTester(mixFile)
		mixFile.entries.each { entry ->

			if (raMixDb) {
				if (entry.id == 0x7fffffff) {
					entries << new MixEntry(mixFile, entry, 'RA-MIXer localDB', null, entry.size, false, 'Local database created by the RA-MIXer tool')
					return
				}

				var dbEntry = raMixDb.entries.find { dbEntry -> dbEntry.id() == entry.id }
				if (dbEntry) {
					entries << new MixEntry(mixFile, entry, dbEntry.name(), dbEntry.name().fileClass, entry.size, false, dbEntry.description())
					return
				}
			}

			// Perform a lookup to see if we know about this file already, getting both a name and class
			var dbEntry = mixDatabase.find(entry.id)
			if (dbEntry) {
				entries << new MixEntry(mixFile, entry, dbEntry.name(), dbEntry.name().fileClass, entry.size)
				return
			}

			// Otherwise try determine what kind of file this is, getting only a class
			var testerResult = mixEntryTester.test(entry)
			if (testerResult) {
				entries << new MixEntry(mixFile, entry, testerResult.name, testerResult.fileClass, entry.size, true)
			}
			else {
				entries << new MixEntry(mixFile, entry, "(unknown entry, ID: 0x${Integer.toHexString(entry.id)})", null, entry.size, true)
			}
		}

		entries.sort()
	}

	/**
	 * Clear the current entry in preview and reset the preview scene.
	 */
	private void clearPreview() {

		selectedFileInputStream?.close()
		removeChild(preview)
		preview = null
		camera.transform.identity()
	}

	/**
	 * Cycle through the available palettes and apply to any paletted objects in
	 * the scene.
	 */
	private void cyclePalette() {

		var paletteComponent = globalPalette.findComponentByType(PaletteComponent)
		paletteComponent.palette = loadPalette(PaletteType.values()[Math.wrap(currentPalette.ordinal() + 1, 0, PaletteType.values().length)])
	}

	/**
	 * Load the given palette as the global palette for objects.
	 */
	private nz.net.ultraq.redhorizon.graphics.Palette loadPalette(PaletteType paletteType = PaletteType.RA_TEMPERATE) {

		logger.info("Using ${paletteType} palette")
		currentPalette = paletteType
		return getResourceAsStream(paletteType.file).withBufferedStream { stream ->
			return new nz.net.ultraq.redhorizon.graphics.Palette(paletteType.file, stream)
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
	 * Update the preview area with the media for the selected file.
	 */
	void preview(File file) {

		logger.info('Loading {}...', file.name)

		var fileClass = file.name.fileClass
		if (fileClass) {
			selectedFileInputStream = file.newInputStream()
			var fileInstance = time("Reading file ${file.name} from filesystem", logger) { ->
				return fileClass.newInstance(selectedFileInputStream)
			}
			preview(fileInstance, file.nameWithoutExtension)
		}
		else {
			logger.info('No filetype implementation for {}', file.name)
		}
	}

	/**
	 * Update the preview area for the given file data and type.
	 */
	void preview(Object file, String objectId) {

		logger.info('File details: {}', file)

		var mediaNode = switch (file) {

		// Objects
//			case ShpFile ->
//				preview(file, objectId)
//			case TmpFileRA ->
//				preview(file, objectId)
//			case IniFile ->
//				preview(file as MapFile, objectId)
//
//				// Media
//			case ImageFile ->
//				new FullScreenContainer().addChild(new Sprite(file))
//			case VideoFile ->
//				new FullScreenContainer().addChild(new Video(file).attachScript(new PlaybackScript(true)))
//			case AnimationFile ->
//				new FullScreenContainer().addChild(new Animation(file).attachScript(new PlaybackScript(true)))
//			case SoundFile ->
//				new Sound(file).attachScript(new PlaybackScript(file.forStreaming))

		// 🤷
			case PalFileDecoder ->
				new Palette(file)
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
