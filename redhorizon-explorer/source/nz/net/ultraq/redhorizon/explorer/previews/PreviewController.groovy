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

import nz.net.ultraq.redhorizon.audio.AudioData
import nz.net.ultraq.redhorizon.audio.AudioSource
import nz.net.ultraq.redhorizon.audio.AudioStoppedEvent
import nz.net.ultraq.redhorizon.audio.StreamingAudioData
import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.filetypes.IniFile
import nz.net.ultraq.redhorizon.classic.maps.RedAlertMapLoader
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.explorer.filedata.FileEntry
import nz.net.ultraq.redhorizon.explorer.filedata.FileTester
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.previews.AnimationPlaybackScript.AnimationStoppedEvent
import nz.net.ultraq.redhorizon.explorer.previews.VideoPlaybackScript.VideoStoppedEvent
import nz.net.ultraq.redhorizon.explorer.ui.EntrySelectedEvent
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.PaletteSwapMap
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.Video
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.resources.ResourceManager
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.RESOURCE_MANAGER

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
class PreviewController extends Script implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(PreviewController)

	private InputStream selectedFileInputStream
	private Object previewedMedia
	private Node previewedEntity

	@Override
	void close() {

		clearPreview(node.scene)
	}

	/**
	 * Clear the current entry in preview and reset the preview scene.
	 */
	private void clearPreview(Scene scene) {

		if (previewedEntity) {
			previewedEntity.close()
			scene.removeChild(previewedEntity)
			previewedEntity = null
		}
		if (previewedMedia && previewedMedia instanceof AutoCloseable) {
			previewedMedia.close()
			previewedMedia = null
		}
		if (selectedFileInputStream) {
			selectedFileInputStream.close()
			selectedFileInputStream = null
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

		scene.find(Camera).resetTransform()
		scene.trigger(new PreviewEndEvent())
	}

	/**
	 * Update the preview area with the media for the selected file.
	 */
	void preview(FileEntry entry) {

		var scene = node.scene

		scene.queueUpdate { ->
			clearPreview(scene)

			var file = entry.file()
			logger.info('Loading {}...', file.name)

			selectedFileInputStream = file.newInputStream()

			// Special case for map files for now (only way to know it's a map file is
			// to read the whole thing!)
			if (file.name.endsWith('.ini')) {
				var iniFile = new IniFile(selectedFileInputStream)
				if (iniFile['Basic']) {
					var resourceManager = RESOURCE_MANAGER.get()
					previewMap(scene, iniFile, file.name, resourceManager)
				}
			}
			else {
				var result = new FileTester().test(file.name, file.size(), selectedFileInputStream)
				if (result) {
					time("Reading file ${file.name} from filesystem took {}ms", logger) { ->
						var decoder = result.decoder().getConstructor().newInstance()
						var media = result.mediaClass().newInstance(file.name, decoder, selectedFileInputStream)
						previewObject(scene, media, file.name)
					}
				}
				else {
					logger.info('No filetype implementation for {}', file.name)
				}
			}

			scene.trigger(new EntrySelectedEvent(entry))
		}
	}

	/**
	 * Update the preview area with the media for the selected mix file entry.
	 */
	void preview(MixEntry entry) {

		var scene = node.scene

		scene.queueUpdate { ->
			clearPreview(scene)

			logger.info('Loading {} from mix file', entry.name())

			selectedFileInputStream = new BufferedInputStream(entry.mixFile().getEntryData(entry.mixEntry()))
			var result = new FileTester().test(entry.name(), entry.size(), selectedFileInputStream)
			if (result) {
				time("Reading file ${entry.name()} from mix file took {}ms", logger) { ->
					var decoder = result.decoder().getConstructor().newInstance()
					var media = result.mediaClass().newInstance(entry.name(), decoder, selectedFileInputStream)
					previewObject(scene, media, entry.name())
				}
			}
			else {
				logger.info('No filetype implementation for {}', entry.name())
			}

			scene.trigger(new EntrySelectedEvent(entry))
		}
	}

	/**
	 * Attempt to load a map from it's .ini file.
	 */
	private void previewMap(Scene scene, IniFile iniFile, String fileName, ResourceManager resourceManager) {

		var mapNode = time("Loading map ${fileName} took {}ms", logger) { ->
			return new RedAlertMapLoader(resourceManager).load(iniFile)
		}
		mapNode
			.addChild(new PaletteSwapMap(Faction.GOLD.colours))
		scene << mapNode
		previewedEntity = mapNode
		scene.trigger(new PreviewBeginEvent(fileName))
	}

	/**
	 * Update the preview area for the given file data and type.
	 */
	private void previewObject(Scene scene, Object file, String fileName) {

		var entity = switch (file) {

		// Dynamic objects
			case SpriteSheet ->
				yield previewSprite(file, fileName)

				// Static media
			case Image -> {
				yield new Node()
					.addChild(new Sprite(file, BasicShader))
					.addChild(new ScriptNode(DarkPreviewScript))
					.withName("Image - ${fileName}")
			}
			case Animation -> {
				// TODO: Is there some better way to convey animations that needs special scaling?
				if (fileName.endsWith('.wsa')) {
					file.scale(2f, 2.4f)
				}
				yield new Node()
					.addChild(file
						.addChild(new ScriptNode(AnimationPlaybackScript))
						.on(AnimationStoppedEvent) { event ->
							scene.queueUpdate { ->
								clearPreview(scene)
							}
						}
					)
					.addChild(new ScriptNode(DarkPreviewScript))
					.withName("Animation - ${fileName}")
			}
			case Video -> {
				if (fileName.endsWith('.vqa')) {
					file.scale(2f, 2.4f)
				}
				yield new Node()
					.addChild(file
						.addChild(new ScriptNode(VideoPlaybackScript))
						.on(VideoStoppedEvent) { event ->
							scene.queueUpdate { ->
								clearPreview(scene)
							}
						}
					)
					.addChild(new ScriptNode(DarkPreviewScript))
					.withName("Video - ${fileName}")
			}
			case AudioData ->
				yield new Node()
					.addChild(new AudioSource(file)
						.addChild(new ScriptNode(SoundPlaybackScript)))
					.withName("Sound - ${fileName}")
			case StreamingAudioData -> {
				yield new Node()
					.addChild(new AudioSource(file)
						.addChild(new ScriptNode(MusicPlaybackScript))
						.on(AudioStoppedEvent) { event ->
							scene.queueUpdate { ->
								clearPreview(scene)
							}
						}
					)
					.withName("Music - ${fileName}")
			}

				// 🤷
			case Palette ->
				yield new PalettePreview(file)
					.withName("Palette - ${fileName}")
			default ->
				logger.info('Filetype of {} not yet configured', file.class.simpleName)
		}

		if (entity) {
			scene.addChild(entity)
			previewedEntity = entity
			scene.trigger(new PreviewBeginEvent(fileName))
		}
		previewedMedia = file
	}

	/**
	 * Attempt to load up an object from its corresponding SHP file.
	 */
	private Node previewSprite(SpriteSheet spriteSheet, String fileName) {

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
		return new Node()
			.addChild(new PaletteSwapMap(Faction.GOLD.colours))
			.addChild(new Sprite(spriteSheet, PalettedSpriteShader))
			.addChild(new ScriptNode(SpritePreviewScript))
			.withName("Sprite - ${fileName}")
	}
}
