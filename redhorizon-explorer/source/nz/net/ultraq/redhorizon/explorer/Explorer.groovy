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
import nz.net.ultraq.redhorizon.classic.filetypes.MapFile
import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.MainMenu.MenuItem
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Animation
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.FullScreenContainer
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sound
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Video
import nz.net.ultraq.redhorizon.explorer.objects.Map
import nz.net.ultraq.redhorizon.explorer.objects.SpriteFrames
import nz.net.ultraq.redhorizon.explorer.scripts.MapViewerScript
import nz.net.ultraq.redhorizon.explorer.scripts.PlaybackScript
import nz.net.ultraq.redhorizon.explorer.scripts.SpriteShowcaseScript
import nz.net.ultraq.redhorizon.explorer.scripts.UnitShowcaseScript
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import imgui.ImGui
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O

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

	private final List<Entry> entries = new CopyOnWriteArrayList<>()
	private final EntryList entryList = new EntryList(entries)
	private final MixDatabase mixDatabase = new MixDatabase()

	private Scene scene
	private File currentDirectory
	private InputStream selectedFileInputStream
	private Palette palette
	private boolean touchpadInput
	private NodeList nodeList

	/**
	 * Constructor, sets up an application with the default configurations.
	 *
	 * @param version
	 * @param palette
	 */
	Explorer(String version, Palette palette) {

		// TODO: Be able to choose which palette to apply to a paletted file
		this.palette = palette

		new Application("Explorer - ${version}")
			.addAudioSystem()
			.addGraphicsSystem(new GraphicsConfiguration(
				clearColour: Colour.GREY,
				maximized: userPreferences.get(ExplorerPreferences.WINDOW_MAXIMIZED),
				renderResolution: renderResolution,
				startWithChrome: true
			), entryList)
			.addTimeSystem()
			.onApplicationStart(this::applicationStart)
			.onApplicationStop(this::applicationStop)
			.start()
	}

	private void applicationStart(Application application, Scene scene) {

		this.scene = scene

		application.on(WindowMaximizedEvent) { event ->
			userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, event.maximized)
		}

		// Also toggle the explorer GUI with the same key for toggling the ImGui chrome
		entryList.toggleWith(scene.inputEventStream, GLFW_KEY_O)
		buildList(new File(System.getProperty("user.dir")))

		// Handle events from the explorer GUI
		entryList.on(EntrySelectedEvent) { event ->
			clearPreview()
			def entry = event.entry
			if (entry instanceof MixEntry) {
				if (entry.name == '..') {
					buildList(currentDirectory)
				}
				else {
					preview(entry)
				}
			}
			else if (entry instanceof FileEntry) {
				def file = entry.file
				if (file.directory) {
					buildList(file)
				}
				else if (file.name.endsWith('.mix')) {
					buildList(new MixFile(file))
				}
				else {
					preview(file)
				}
			}
		}

		nodeList = new NodeList(scene)
		scene.addImGuiElement(nodeList)

		// Add a menu item for touchpad input
		scene.gameMenu.optionsMenu << new MenuItem() {
			@Override
			void render() {
				if (ImGui.menuItem('Touchpad input', null, touchpadInput)) {
					touchpadInput = !touchpadInput
					Map mapNode = scene.findNode { node -> node instanceof Map }
					if (mapNode) {
						((MapViewerScript)mapNode.script).touchpadInput = touchpadInput
					}
				}
			}
		}
	}

	@SuppressWarnings('unused')
	private void applicationStop(Application application, Scene scene) {

		clearPreview()
	}

	/**
	 * Update the contents of the list from the current directory.
	 *
	 * @param directory
	 */
	private void buildList(File directory) {

		entries.clear()

		if (directory.parent) {
			entries << new FileEntry(directory.parentFile, '/..')
		}
		directory
			.listFiles()
			.sort { file1, file2 ->
				file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
						file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				entries << new FileEntry(fileOrDirectory)
			}

		currentDirectory = directory
	}

	/**
	 * Update the contents of the list from the current mix file.
	 *
	 * @param mixFile
	 */
	private void buildList(MixFile mixFile) {

		entries.clear()
		entries << new MixEntry(mixFile, null, '..')

		def mixEntryTester = new MixEntryTester(mixFile)
		mixFile.entries.each { entry ->

			// Perform a lookup to see if we know about this file already, getting both a name and class
			def matchingData = mixDatabase.find(entry.id)
			if (matchingData) {
				entries << new MixEntry(mixFile, entry, matchingData.name, matchingData.name.fileClass)
			}

			// Otherwise try determine what kind of file this is, getting only a class
			else {
				def testerResult = mixEntryTester.test(entry)
				if (testerResult) {
					entries << new MixEntry(mixFile, entry, testerResult.name, null, testerResult.file, true)
				}
				else {
					entries << new MixEntry(mixFile, entry, "(unknown entry, ID: 0x${Integer.toHexString(entry.id)})", null, null, true)
				}
			}
		}

		entries.sort()
	}

	/**
	 * Clear the current entry in preview and reset the preview scene.
	 */
	private void clearPreview() {

		selectedFileInputStream?.close()
		scene.clear()
		scene.camera.center(new Vector3f())
	}

	/**
	 * Update the preview area with the media for the selected mix file entry.
	 */
	private void preview(MixEntry entry) {

		logger.info('Loading {} from mix file', entry.name ?: '(unknown)')

		var file = entry.file
		var fileClass = entry.fileClass
		var entryId = entry.name.substring(0, entry.name.indexOf('.'))

		if (file) {
			selectedFileInputStream = entry.mixFile.getEntryData(entry.mixEntry)
			preview(file, entryId)
		}
		else if (fileClass) {
			selectedFileInputStream = entry.mixFile.getEntryData(entry.mixEntry)
			preview(fileClass.newInstance(selectedFileInputStream), entryId)
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

		def fileClass = file.name.fileClass
		if (fileClass) {
			selectedFileInputStream = file.newInputStream()
			preview(fileClass.newInstance(selectedFileInputStream), file.nameWithoutExtension)
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
			case MapFile ->
				preview(file, objectId)

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
			default ->
				logger.info('Filetype of {} not yet configured', file.class.simpleName)
		}

		if (mediaNode) {
			scene << mediaNode
		}
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
				case 'infantry', 'structure', 'vehicle' -> Unit
				default -> logger.info('Unit type {} not supported', unitData.type)
			}
			if (targetClass) {
				var unit = targetClass
					.getDeclaredConstructor(ImagesFile, Palette, UnitData)
					.newInstance(shpFile, palette, unitData)
					.attachScript(new UnitShowcaseScript())
				scene << unit
			}
		}

		// No config found, fall back to viewing a SHP file as media
		else {
			var palettedSprite = new SpriteFrames(shpFile, palette)
				.attachScript(new SpriteShowcaseScript())
			scene << palettedSprite
		}
	}

	/**
	 * Attempt to load up a map from its map file.
	 */
	private void preview(MapFile mapFile, String objectId) {

		// Assume the directory in which file resides is where we can search for items
		new ResourceManager(currentDirectory,
			'nz.net.ultraq.redhorizon.filetypes',
			'nz.net.ultraq.redhorizon.classic.filetypes').withCloseable { resourceManager ->

			scene << new Map(mapFile, resourceManager).attachScript(new MapViewerScript(touchpadInput))
		}
	}
}
