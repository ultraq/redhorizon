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
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.MainMenu.MenuItem
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
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
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.Runtime
import nz.net.ultraq.redhorizon.runtime.VersionReader

import imgui.ImGui
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.IDefaultValueProvider
import picocli.CommandLine.Model.ArgSpec
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import static org.lwjgl.glfw.GLFW.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A Command & Conquer asset explorer, allows peeking into and previewing the
 * classic C&C files using a file explorer-like interface.
 *
 * @author Emanuel Rabina
 */
class Explorer implements Application {

	private static final Logger logger = LoggerFactory.getLogger(Explorer)
	private static final Dimension renderResolution = new Dimension(1280, 800)
	private static final float volume = 0.5f
	private static final Preferences userPreferences = new Preferences()

	/**
	 * Entry point to the Explorer application.
	 */
	static void main(String[] args) {

		System.exit(new CommandLine(new CliWrapper()).execute(args))
	}

	/**
	 * Not-so-tiny CLI wrapper around the Explorer application so it's launchable
	 * w/ Picocli.
	 */
	@Command(name = 'explorer', defaultValueProvider = DefaultOptionsProvider)
	static class CliWrapper implements Callable<Integer> {

		@Parameters(index = '0', defaultValue = Option.NULL_VALUE, description = 'Path to a file to open on launch')
		File file

		@Option(names = '--maximized', description = 'Start the application maximized. Remembers your last usage.')
		boolean maximized

		@Option(names = '--touchpad-input', description = 'Start the application using touchpad controls.  Remembers your last usage.')
		boolean touchpadInput

		@Override
		Integer call() {

			var entries = new CopyOnWriteArrayList<Entry>()
			var entryList = new EntryList(entries)
			var nodeList = new NodeList()
			var options = new ExplorerOptions(
				maximized: maximized,
				touchpadInput: touchpadInput
			)

			var exitCode = new Runtime(new Explorer(entries, entryList, nodeList, options, file))
				.withGraphicsConfiguration(new GraphicsConfiguration(
					clearColour: Colour.GREY,
					maximized: options.maximized,
					renderResolution: renderResolution,
					startWithChrome: true
				))
				.withImGuiElements(new LogPanel(true), entryList, nodeList)
				.execute()

			// Save preferences for next time
			userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, options.maximized)
			userPreferences.set(ExplorerPreferences.TOUCHPAD_INPUT, options.touchpadInput)

			return exitCode
		}

		/**
		 * Provide default options for the user-remembered options.
		 */
		static class DefaultOptionsProvider implements IDefaultValueProvider {

			@Override
			String defaultValue(ArgSpec argSpec) {

				if (argSpec.option) {
					var option = (OptionSpec)argSpec
					if (option.longestName() == '--maximized') {
						return userPreferences.get(ExplorerPreferences.WINDOW_MAXIMIZED)
					}
					if (option.longestName() == '--touchpad-input') {
						return userPreferences.get(ExplorerPreferences.TOUCHPAD_INPUT).toString()
					}
				}
				return null
			}
		}
	}

	final String name = 'Explorer'
	final String version = new VersionReader('runtime.properties').read()

	private final ExplorerOptions options
	private final List<Entry> entries
	private final EntryList entryList
	private final NodeList nodeList
	private final File openOnLaunch
	private final ResourceManager resourceManager = new ResourceManager(
		new File(System.getProperty('user.dir'), 'mix'),
		'nz.net.ultraq.redhorizon.filetypes',
		'nz.net.ultraq.redhorizon.classic.filetypes')
	private final MixDatabase mixDatabase = new MixDatabase()

	private Scene scene
	private Camera camera
	private Listener listener
	private GlobalPalette globalPalette
	private Node previewNode
	private File currentDirectory
	private InputStream selectedFileInputStream
	private PaletteType currentPalette
	private List<RemoveControlFunction> removeControlFunctions = []

	/**
	 * Constructor, sets up an application with the default configurations.
	 *
	 * @param openOnLaunch
	 *   Optional, a file to open on launch of the explorer.
	 */
	Explorer(List<Entry> entries, EntryList entryList, NodeList nodeList, ExplorerOptions options, File openOnLaunch) {

		this.entries = entries
		this.entryList = entryList
		this.nodeList = nodeList
		this.options = options
		this.openOnLaunch = openOnLaunch
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

		var sprite = new PalettedSprite(imagesFile).attachScript(new SpriteShowcaseScript(camera))
		sprite.bounds { ->
			center()
		}
		sprite.name = "PalettedSprite - ${objectId}"
		scene << sprite
		previewNode = sprite
	}

	/**
	 * Attempt to load up an object from its corresponding SHP file.
	 */
	private void preview(ShpFile shpFile, String objectId) {

		String unitConfig
		try {
			unitConfig = getUnitDataJson(objectId)
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
			.attachScript(new SpriteShowcaseScript(camera))
		tile.bounds.center()
		tile.name = "PalettedSprite - ${objectId}"
		scene << tile
		previewNode = tile
	}

	/**
	 * Attempt to load up a map from its map file.
	 */
	private void preview(MapFile mapFile, String objectId) {

		var mapViewerScript = new MapViewerScript(camera, nodeList, options.touchpadInput)
		time("Loading map ${objectId}", logger) { ->
			resourceManager.withDirectory(currentDirectory) { ->
				var map = new Map(mapFile, resourceManager).attachScript(mapViewerScript)
				scene << map
				previewNode = map
			}
		}
		mapViewerScript.viewInitialPosition()
	}

	@Override
	void start(Scene scene) {

		this.scene = scene

		on(WindowMaximizedEvent) { event ->
			options.maximized = event.maximized
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
				if (ImGui.menuItem('Touchpad input', null, options.touchpadInput)) {
					options.touchpadInput = !options.touchpadInput
					var mapNode = (Map)scene.findDescendent { node -> node instanceof Map }
					if (mapNode) {
						((MapViewerScript)mapNode.script).touchpadInput = options.touchpadInput
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
		removeControlFunctions.addAll(scene.inputRequestHandler.addControls(
			new KeyControl(GLFW_KEY_P, 'Cycle palette', { -> cyclePalette() }),
			new KeyControl(GLFW_KEY_UP, 'Select previous', { -> entryList.selectPrevious() }),
			new KeyControl(GLFW_KEY_DOWN, 'Select next', { -> entryList.selectNext() }))
		)

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

	@Override
	void stop(Scene scene) {

		removeControlFunctions*.remove()
		clearPreview()
		scene.clear()
		resourceManager.close()
	}
}
