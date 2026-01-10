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
import nz.net.ultraq.redhorizon.audio.Sound
import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.graphics.FactionComponent
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.audio.MusicComponent
import nz.net.ultraq.redhorizon.engine.audio.SoundComponent
import nz.net.ultraq.redhorizon.engine.graphics.AnimationComponent
import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.graphics.VideoComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.explorer.FileEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.previews.AnimationPlaybackScript.AnimationStoppedEvent
import nz.net.ultraq.redhorizon.explorer.previews.MusicPlaybackScript.MusicStoppedEvent
import nz.net.ultraq.redhorizon.explorer.previews.VideoPlaybackScript.VideoStoppedEvent
import nz.net.ultraq.redhorizon.explorer.ui.EntrySelectedEvent
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.Video
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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

			// Animate back to the origin
//			var translateStart = new Vector3f(scene.camera.position)
//			var translateEnd = new Vector3f()
//			var translateResult = new Vector3f()
//			var scaleStart = scene.camera.scale
//			var scaleEnd = new Vector3f(1f, 1f, 1f)
//			var scaleResult = new Vector3f()
//			var resetResult = new Matrix4f()
//			return new Transition(EasingFunctions::easeOutSine, 400, { float value ->
//				// start + ((end - start) * value)
//				translateEnd.sub(translateStart, translateEnd).mul(value).add(translateStart)
//				scaleEnd.sub(scaleStart, scaleResult).mul(value).add(scaleStart)
//				this.scene.camera.setTransform(resetResult.identity()
//					.translate(translateResult)
//					.scale(scaleResult))
//			})
//				.start()
//				.thenRunAsync { ->
//					scene.trigger(new PreviewEndEvent())
//				}

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
				if (entry instanceof FileEntry) {
					var file = entry.file
					if (file.file && !file.name.endsWith('.mix')) {
						scene.queueUpdate { ->
							clearPreview()
							preview(file)
						}
					}
				}
				else if (entry instanceof MixEntry) {
					scene.queueUpdate { ->
						clearPreview()
						preview(entry)
					}
				}
			}
		}

		/**
		 * Update the preview area with the media for the selected file.
		 */
		private void preview(File file) {

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
		private void preview(MixEntry entry) {

			logger.info('Loading {} from mix file', entry.name ?: '(unknown)')

			var fileClass = entry.fileClass
			if (fileClass) {
				selectedFileInputStream = new BufferedInputStream(entry.mixFile.getEntryData(entry.mixEntry))
				var fileInstance = time("Reading file ${entry.name} from Mix file", logger) { ->
					return fileClass.newInstance(entry.name, selectedFileInputStream)
				}
				preview(fileInstance, entry.name)
			}
			else {
				logger.info('No filetype implementation for {}', entry.name ?: '(unknown)')
			}
		}

		/**
		 * Update the preview area for the given file data and type.
		 */
		private void preview(Object file, String fileName) {

			var entity = switch (file) {

			// Objects
				case SpriteSheet ->
					previewObject(file, fileName)
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
						.withName("Image - ${fileName}")
				case Animation -> {
					var animationComponent = new AnimationComponent(file)
					// TODO: Is there some better way to convey animations that needs special scaling?
					if (fileName.endsWith('.wsa')) {
						animationComponent.scale(2f, 2.4f)
					}
					yield new Entity()
						.addComponent(animationComponent)
						.addComponent(new ScriptComponent(DarkPreviewScript))
						.addComponent(new ScriptComponent(AnimationPlaybackScript))
						.withName("Animation - ${fileName}")
						.on(AnimationStoppedEvent) { event ->
							scene.queueUpdate { ->
								clearPreview()
							}
						}
				}
				case Video -> {
					var videoComponent = new VideoComponent(file)
					if (fileName.endsWith('.vqa')) {
						videoComponent.scale(2f, 2.4f)
					}
					yield new Entity()
						.addComponent(videoComponent)
						.addComponent(new ScriptComponent(DarkPreviewScript))
						.addComponent(new ScriptComponent(VideoPlaybackScript))
						.withName("Video - ${fileName}")
						.on(VideoStoppedEvent) { event ->
							scene.queueUpdate { ->
								clearPreview()
							}
						}
				}
				case Sound ->
					new Entity()
						.addComponent(new SoundComponent(file))
						.addComponent(new ScriptComponent(SoundPlaybackScript))
						.withName("Sound - ${fileName}")
				case Music ->
					new Entity()
						.addComponent(new MusicComponent(file))
						.addComponent(new ScriptComponent(MusicPlaybackScript))
						.withName("Music - ${fileName}")
						.on(MusicStoppedEvent) { event ->
							scene.queueUpdate { ->
								clearPreview()
							}
						}

					// 🤷
				case Palette ->
					new PalettePreview(file)
						.withName("Palette - ${fileName}")
				default ->
					logger.info('Filetype of {} not yet configured', file.class.simpleName)
			}

			if (entity) {
				scene.addChild(entity)
				preview = entity
				scene.trigger(new PreviewBeginEvent(fileName))
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
		private Entity previewObject(SpriteSheet spriteSheet, String fileName) {

			var objectId = fileName.substring(0, fileName.lastIndexOf('.'))
			String unitConfig
			try {
				unitConfig = getUnitDataJson(objectId)
				logger.info('Configuration data:\n{}', JsonOutput.prettyPrint(unitConfig).replaceAll(' {4}', '  '))
			}
			catch (IllegalArgumentException ignored) {
				logger.info('No configuration available for {}', objectId)
			}

			// Found a unit config, use it to view the file
			if (unitConfig) {
				var unitData = new JsonSlurper().parseText(unitConfig) as UnitData
				return switch (unitData.type) {
					case 'infantry', 'structure', 'vehicle', 'aircraft' ->
						new UnitPreview(spriteSheet, unitData)
							.withName("Unit - ${objectId}")
					default -> {
						logger.info('Unit type {} not supported', unitData.type)
						yield null
					}
				}
			}

			// No config found, fall back to viewing a SHP file as media
			else {
				return new Entity()
					.addComponent(new FactionComponent(Faction.GOLD))
					.addComponent(new SpriteComponent(spriteSheet, PalettedSpriteShader))
					.addComponent(new ScriptComponent(SpritePreviewScript))
					.withName("Sprite - ${fileName}")
			}
		}

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
