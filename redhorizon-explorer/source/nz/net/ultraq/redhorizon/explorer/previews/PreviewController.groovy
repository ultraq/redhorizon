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
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.explorer.filedata.FileEntry
import nz.net.ultraq.redhorizon.explorer.filedata.FileTester
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
import groovy.transform.TupleConstructor

/**
 * Controls the previewing of file assets in the main scene.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class PreviewController extends Entity<PreviewController> implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(PreviewController)

	ExplorerScene scene
	private InputStream selectedFileInputStream
	private Entity previewedEntity

	@Override
	void close() {

		clearPreview()
	}

	/**
	 * Clear the current entry in preview and reset the preview scene.
	 */
	void clearPreview() {

		selectedFileInputStream?.close()
		if (previewedEntity) {
			scene.removeChild(previewedEntity)
			previewedEntity.close()
			previewedEntity = null
		}

		// Animate back to the origin
//		var translateStart = new Vector3f(scene.camera.position)
//		var translateEnd = new Vector3f()
//		var translateResult = new Vector3f()
//		var scaleStart = scene.camera.scale
//		var scaleEnd = new Vector3f(1f, 1f, 1f)
//		var scaleResult = new Vector3f()
//		var resetResult = new Matrix4f()
//		return new Transition(EasingFunctions::easeOutSine, 400, { float value ->
//			// start + ((end - start) * value)
//			translateEnd.sub(translateStart, translateEnd).mul(value).add(translateStart)
//			scaleEnd.sub(scaleStart, scaleResult).mul(value).add(scaleStart)
//			this.scene.camera.setTransform(resetResult.identity()
//				.translate(translateResult)
//				.scale(scaleResult))
//		})
//			.start()
//			.thenRunAsync { ->
//				scene.trigger(new PreviewEndEvent())
//			}

		scene.camera.resetTransform()
		scene.trigger(new PreviewEndEvent())
	}

	/**
	 * Update the preview area with the media for the selected file.
	 */
	void preview(FileEntry entry) {

		var file = entry.file()
		logger.info('Loading {}...', file.name)

		selectedFileInputStream = file.newInputStream()
		var result = new FileTester().test(file.name, file.size(), selectedFileInputStream)
		if (result) {
			time("Reading file ${file.name} from filesystem", logger) { ->
				var decoder = result.decoder().getConstructor().newInstance()
				var media = result.mediaClass().newInstance(file.name, decoder, selectedFileInputStream)
				previewObject(media, file.name)
			}
		}
		else {
			logger.info('No filetype implementation for {}', file.name)
		}

		scene.trigger(new EntrySelectedEvent(entry))
	}

	/**
	 * Update the preview area with the media for the selected mix file entry.
	 */
	void preview(MixEntry entry) {

		logger.info('Loading {} from mix file', entry.name())

		selectedFileInputStream = new BufferedInputStream(entry.mixFile().getEntryData(entry.mixEntry()))
		var result = new FileTester().test(null, entry.size(), selectedFileInputStream)
		if (result) {
			time("Reading file ${entry.name()} from mix file", logger) { ->
				var decoder = result.decoder().getConstructor().newInstance()
				var media = result.mediaClass().newInstance(entry.name(), decoder, selectedFileInputStream)
				previewObject(media, entry.name())
			}
		}
		else {
			logger.info('No filetype implementation for {}', entry.name())
		}

		scene.trigger(new EntrySelectedEvent(entry))
	}

	/**
	 * Update the preview area for the given file data and type.
	 */
	private void previewObject(Object file, String fileName) {

		var entity = switch (file) {

		// Dynamic objects
			case SpriteSheet ->
				previewSprite(file, fileName)
//		case IniFile ->
//			preview(file as MapFile, objectId)
//
				// Static media
			case Image -> {
//				if (fileClass == TmpFileRADecoder) {
//					yield new Entity()
//						.addComponent(new SpriteComponent(file, PalettedSpriteShader))
//						.withName("Tilemap - ${fileName}")
//				}
				yield new Entity()
					.addComponent(new SpriteComponent(file, BasicShader))
					.addComponent(new ScriptComponent(DarkPreviewScript))
					.withName("Image - ${fileName}")
			}
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
			previewedEntity = entity
			scene.trigger(new PreviewBeginEvent(fileName))
		}
	}

	/**
	 * Attempt to load up an object from its corresponding SHP file.
	 */
	private Entity previewSprite(SpriteSheet spriteSheet, String fileName) {

		var indexOfDot = fileName.lastIndexOf('.')
		var objectId = indexOfDot != -1 ? fileName.substring(0, indexOfDot) : fileName
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

		// No config found, fall back to viewing a SHP file as frame-by-frame media
		return new Entity()
			.addComponent(new FactionComponent(Faction.GOLD))
			.addComponent(new SpriteComponent(spriteSheet, PalettedSpriteShader))
			.addComponent(new ScriptComponent(SpritePreviewScript))
			.withName("Sprite - ${fileName}")
	}

	/**
	 * Attempt to load up a map from its map file.
	 */
//	private void previewMap(MapFile mapFile, String objectId) {
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
