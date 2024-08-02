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

package nz.net.ultraq.redhorizon.explorer

import nz.net.ultraq.preferences.Preferences
import nz.net.ultraq.redhorizon.classic.filetypes.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.MapFile
import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.RaMixDatabase
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileRA
import nz.net.ultraq.redhorizon.classic.maps.Map
import nz.net.ultraq.redhorizon.classic.nodes.GlobalPalette
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.MainMenu.MenuItem
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Animation
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.FullScreenContainer
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.GridLines
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Listener
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sound
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Video
import nz.net.ultraq.redhorizon.events.RemoveEventFunction
import nz.net.ultraq.redhorizon.explorer.objects.Palette
import nz.net.ultraq.redhorizon.explorer.scripts.MapViewerScript
import nz.net.ultraq.redhorizon.explorer.scripts.PlaybackScript
import nz.net.ultraq.redhorizon.explorer.scripts.SpriteShowcaseScript
import nz.net.ultraq.redhorizon.explorer.scripts.UnitShowcaseScript
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import imgui.ImGui
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A Command & Conquer asset explorer, allows peeking into and previewing the
 * classic C&C files using a file explorer-like interface.
 *
 * @author Emanuel Rabina
 */
class Explorer {

	private static final Logger logger = LoggerFactory.getLogger(Explorer)
	private static final Preferences userPreferences = new Preferences()
	private static final Dimension renderResolution = new Dimension(1280, 800)
	private static final float volume = 0.5f

	private final List<Entry> entries = new CopyOnWriteArrayList<>()
	private final EntryList entryList = new EntryList(entries)
	private final MixDatabase mixDatabase = new MixDatabase()
	private final NodeList nodeList = new NodeList()
	private final ResourceManager resourceManager = new ResourceManager(
		new File(System.getProperty('user.dir'), 'mix'),
		'nz.net.ultraq.redhorizon.filetypes',
		'nz.net.ultraq.redhorizon.classic.filetypes')

	private Scene scene
	private Camera camera
	private Listener listener
	private GlobalPalette globalPalette
	private Node previewNode
	private File currentDirectory
	private InputStream selectedFileInputStream
	private PaletteType currentPalette
	private boolean touchpadInput
	private List<RemoveEventFunction> removeEventFunctions = []

	/**
	 * Constructor, sets up an application with the default configurations.
	 *
	 * @param version
	 * @param openOnLaunch
	 *   Optional, a file to open on launch of the explorer.
	 */
	Explorer(String version, File openOnLaunch) {

		touchpadInput = userPreferences.get(ExplorerPreferences.TOUCHPAD_INPUT)

		new Application('Explorer', version)
			.addAudioSystem()
			.addGraphicsSystem(new GraphicsConfiguration(
				clearColour: Colour.GREY,
				maximized: userPreferences.get(ExplorerPreferences.WINDOW_MAXIMIZED),
				renderResolution: renderResolution,
				startWithChrome: true
			), new LogPanel(true), entryList, nodeList)
			.addTimeSystem()
			.onApplicationStart { application, scene ->
				applicationStart(application, scene, openOnLaunch)
			}
			.onApplicationStop { application, scene ->
				applicationStop(scene)
			}
			.start()
	}

	private void applicationStart(Application application, Scene scene, File openOnLaunch) {

		this.scene = scene

		application.on(WindowMaximizedEvent) { event ->
			userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, event.maximized)
		}

		buildList(new File(System.getProperty("user.dir")))

		// Handle events from the explorer GUI
		entryList.on(EntrySelectedEvent) { event ->
			var entry = event.entry
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

		nodeList.scene = scene

		// Add a menu item for touchpad input
		scene.gameMenu.optionsMenu << new MenuItem() {
			@Override
			void render() {
				if (ImGui.menuItem('Touchpad input', null, touchpadInput)) {
					touchpadInput = !touchpadInput
					userPreferences.set(ExplorerPreferences.TOUCHPAD_INPUT, touchpadInput)
					var mapNode = (Map)scene.findDescendent { node -> node instanceof Map }
					if (mapNode) {
						((MapViewerScript)mapNode.script).touchpadInput = touchpadInput
					}
				}
			}
		}
		scene.gameMenu.optionsMenu << new MenuItem() {
			@Override
			void render() {
				if (ImGui.menuItem('Cycle palette', 'P')) {
					cyclePalette()
				}
			}
		}
		removeEventFunctions << scene.inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
					case GLFW_KEY_P -> cyclePalette()
					case GLFW_KEY_UP -> entryList.selectPrevious()
					case GLFW_KEY_DOWN -> entryList.selectNext()
				}
			}
		}

		// Start all the global/helper nodes
		camera = new Camera(renderResolution)
		scene << camera

		listener = new Listener(volume)
		scene << listener

		globalPalette = new GlobalPalette(loadPalette())
		scene << globalPalette

		scene << new GridLines(Map.MAX_BOUNDS, 24)

		if (openOnLaunch) {
			preview(openOnLaunch)
		}
	}

	private void applicationStop(Scene scene) {

		removeEventFunctions*.remove()
		clearPreview()
		scene.clear()
		resourceManager.close()
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
					type: fileOrDirectory.file ? fileOrDirectory.name.fileClass?.simpleName : null
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
				entries << new MixEntry(mixFile, entry, dbEntry.name, dbEntry.name.fileClass, entry.size)
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
		scene.removeChild(previewNode)
		previewNode = null
		camera.reset()
	}

	/**
	 * Cycle through the available palettes and apply to any paletted objects in
	 * the scene.
	 */
	private void cyclePalette() {

		globalPalette.palette = loadPalette(PaletteType.values()[Math.wrap(currentPalette.ordinal() + 1, 0, PaletteType.values().length)])
	}

	/**
	 * Load the given palette as the global palette for objects.
	 */
	private nz.net.ultraq.redhorizon.filetypes.Palette loadPalette(PaletteType paletteType = PaletteType.RA_TEMPERATE) {

		logger.info("Using ${paletteType} palette")
		currentPalette = paletteType
		return getResourceAsStream(paletteType.file).withBufferedStream { inputStream ->
			return new PalFile(inputStream)
		}
	}

	/**
	 * Update the preview area with the media for the selected mix file entry.
	 */
	private void preview(MixEntry entry) {

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
	private void preview(File file) {

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
	private void preview(Object file, String objectId) {

		logger.info('File details: {}', file)

		var mediaNode = switch (file) {

		// Objects
			case ShpFile ->
				preview(file, objectId)
			case TmpFileRA ->
				preview(file, objectId)
			case IniFile ->
				preview(file as MapFile, objectId)

				// Media
			case ImageFile ->
				new FullScreenContainer().addChild(new Sprite(file))
			case VideoFile ->
				new FullScreenContainer().addChild(new Video(file).attachScript(new PlaybackScript(true)))
			case AnimationFile ->
				new FullScreenContainer().addChild(new Animation(file).attachScript(new PlaybackScript(true)))
			case SoundFile ->
				new Sound(file).attachScript(new PlaybackScript(file.forStreaming))

				// 🤷
			case PalFile ->
				new Palette(file)
			default ->
				logger.info('Filetype of {} not yet configured', file.class.simpleName)
		}

		if (mediaNode) {
			scene << mediaNode
			previewNode = mediaNode
		}
	}

	/**
	 * Load up any unspecified multi-image file as a sprite to flip through its
	 * frames.
	 */
	private void preview(ImagesFile imagesFile, String objectId) {

		var sprite = new PalettedSprite(imagesFile).attachScript(new SpriteShowcaseScript(camera)).tap {
			bounds { ->
				center()
			}
			name = "PalettedSprite - ${objectId}"
		}
		scene << sprite
		previewNode = sprite
	}

	/**
	 * Attempt to load up an object from its corresponding SHP file.
	 */
	private void preview(ShpFile shpFile, String objectId) {

		String unitConfig
		try {
			unitConfig = getResourceAsText("nz/net/ultraq/redhorizon/classic/units/data/${objectId.toLowerCase()}.json")
			logger.info('Configuration data:\n{}', JsonOutput.prettyPrint(unitConfig))
		}
		catch (IllegalArgumentException ignored) {
			logger.info('No configuration available for {}', objectId)
		}

		// Found a unit config, use it to view the file
		if (unitConfig) {
			var unitData = new JsonSlurper().parseText(unitConfig) as UnitData
			var targetClass = switch (unitData.type) {
				case 'infantry', 'structure', 'vehicle', 'aircraft' -> Unit
				default -> logger.info('Unit type {} not supported', unitData.type)
			}
			if (targetClass) {
				var unit = targetClass
					.getDeclaredConstructor(ImagesFile, UnitData)
					.newInstance(shpFile, unitData)
					.attachScript(new UnitShowcaseScript(camera))
				unit.body.bounds { ->
					center()
				}
				if (unit.turret) {
					unit.turret.bounds { ->
						center()
					}
				}
				scene << unit
				previewNode = unit
			}
		}

		// No config found, fall back to viewing a SHP file as media
		else {
			preview(shpFile as ImagesFile, objectId)
		}
	}

	/**
	 * Load up a tile file and arrange it so that it looks complete.
	 */
	private void preview(TmpFileRA tileFile, String objectId) {

		var singleImageData = tileFile.imagesData.combine(tileFile.width, tileFile.height, tileFile.format, tileFile.tilesX)
		var singleImageWidth = tileFile.tilesX * tileFile.width
		var singleImageHeight = tileFile.tilesY * tileFile.height

		var tile = new PalettedSprite(singleImageWidth, singleImageHeight, 1, 1f, 1f, { scene ->
			return scene.requestCreateOrGet(new SpriteSheetRequest(singleImageWidth, singleImageHeight, tileFile.format, singleImageData))
		})
			.attachScript(new SpriteShowcaseScript(camera)).tap {
			bounds.center()
			name = "PalettedSprite - ${objectId}"
		}
		scene << tile
		previewNode = tile
	}

	/**
	 * Attempt to load up a map from its map file.
	 */
	private void preview(MapFile mapFile, String objectId) {

		var mapViewerScript = new MapViewerScript(camera, nodeList, touchpadInput)
		time("Loading map ${objectId}", logger) { ->
			resourceManager.withDirectory(currentDirectory) { ->
				var map = new Map(mapFile, resourceManager).attachScript(mapViewerScript)
				scene << map
				previewNode = map
			}
		}
		mapViewerScript.viewInitialPosition()
	}
}
