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

package nz.net.ultraq.redhorizon.explorer.previews

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.audio.Music
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.audio.MusicComponent
import nz.net.ultraq.redhorizon.engine.graphics.AnimationComponent
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.explorer.FileEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.previews.AnimationPlaybackScript.AnimationStoppedEvent
import nz.net.ultraq.redhorizon.explorer.previews.MusicPlaybackScript.MusicStoppedEvent
import nz.net.ultraq.redhorizon.explorer.ui.EntrySelectedEvent
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Controls the previewing of file assets in the main scene.
 *
 * @author Emanuel Rabina
 */
class PreviewController extends Entity<PreviewController> implements EventTarget<PreviewController> {

	private static final Logger logger = LoggerFactory.getLogger(PreviewController)

	/**
	 * Constructor, creates a script which listens for events on the scene to load
	 * objects in the preview area.
	 */
	PreviewController() {

		addComponent(new ScriptComponent(PreviewControllerScript))
	}

	static class PreviewControllerScript extends EntityScript<PreviewController> implements AutoCloseable {

		private ExplorerScene scene
		private InputStream selectedFileInputStream
		private Entity preview

		/**
		 * Clear the current entry in preview and reset the preview scene.
		 */
		private void clearPreview() {

			selectedFileInputStream?.close()
			if (preview) {
				scene.removeChild(preview)
				preview.close()
				preview = null
			}
			scene.camera.resetTransform()
			scene.trigger(new PreviewEndEvent())
		}

		@Override
		void close() {

			clearPreview()
		}

		@Override
		void init() {

			scene = entity.scene as ExplorerScene
			scene.on(EntrySelectedEvent) { event ->
				var entry = event.entry()
				if (entry instanceof FileEntry && entry.file.file) {
					scene.queueChange { ->
						clearPreview()
						preview(entry.file)
					}
				}
				else if (entry instanceof MixEntry) {
					scene.queueChange { ->
						clearPreview()
						preview(entry)
					}
				}
			}
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
				preview(fileInstance, file.name)
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
					new Entity()
						.addComponent(new SpriteComponent(file, BasicShader))
						.addComponent(new ScriptComponent(DarkPreviewScript))
						.withName("Image - ${objectId}")
				case Animation -> {
					var animationComponent = new AnimationComponent(file)
					// TODO: Is there some better way to convey animations that needs special scaling?
					if (objectId.endsWith('.wsa')) {
						animationComponent.scale(2f, 2.4f)
					}
					yield new Entity()
						.addComponent(animationComponent)
						.addComponent(new ScriptComponent(DarkPreviewScript))
						.addComponent(new ScriptComponent(AnimationPlaybackScript))
						.withName("Animation - ${objectId}")
						.on(AnimationStoppedEvent) { event ->
							scene.queueChange { ->
								clearPreview()
							}
						}
				}
//			case VideoFile ->
//				new FullScreenContainer().addChild(new Video(file).attachScript(new PlaybackScript(true)))
				case Music ->
					new Entity()
						.addComponent(new MusicComponent(file))
						.addComponent(new ScriptComponent(MusicPlaybackScript))
						.withName("Music - ${objectId}")
						.on(MusicStoppedEvent) { event ->
							scene.queueChange { ->
								clearPreview()
							}
						}
//			case SoundFile ->
//				new Sound(file).attachScript(new PlaybackScript(file.forStreaming))

					// 🤷
				case Palette ->
					new PalettePreview(file)
						.withName("Palette - ${objectId}")
				default ->
					logger.info('Filetype of {} not yet configured', file.class.simpleName)
			}

			if (mediaNode) {
				scene.addChild(mediaNode)
				preview = mediaNode
				scene.trigger(new PreviewBeginEvent(objectId))
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
}
