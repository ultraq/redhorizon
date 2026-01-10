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

package nz.net.ultraq.redhorizon.explorer.extensions

import nz.net.ultraq.redhorizon.audio.Music
import nz.net.ultraq.redhorizon.audio.Sound
import nz.net.ultraq.redhorizon.classic.filetypes.AudFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.CpsFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.PcxFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileRADecoder
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileTDDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.VqaFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.WsaFileDecoder
import nz.net.ultraq.redhorizon.explorer.mixdata.MixData
import nz.net.ultraq.redhorizon.explorer.mixdata.RaMixEntry
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.Video

/**
 * Add convenience methods/properties to objects that represent files.
 *
 * @author Emanuel Rabina
 */
class FileMappingExtensions {

	// TODO: A lot of this mapping could be properties on the decoder classes?

	static final Map<Class, String> DECODER_TO_EXTENSION = [
		(AudFileDecoder): 'aud',
		(CpsFileDecoder): 'cps',
		(PcxFileDecoder): 'pcx',
		(ShpFileDecoder): 'shp',
		(TmpFileRADecoder): 'sno',
		(TmpFileTDDecoder): 'tem',
		(VqaFileDecoder): 'vqa',
		(WsaFileDecoder): 'wsa'
	]

	static final Map<Class, Class> DECODER_TO_MEDIA_CLASS = [
		(AudFileDecoder): Sound,
		(CpsFileDecoder): Image,
		(PcxFileDecoder): Image,
		(ShpFileDecoder): SpriteSheet,
		(TmpFileRADecoder): SpriteSheet,
		(TmpFileTDDecoder): SpriteSheet,
		(VqaFileDecoder): Video,
		(WsaFileDecoder): Animation
	]

	static final Map<String, Closure<Class>> FILE_EXTENSION_TO_MEDIA_CLASS = [
		'aud': { file ->
			// Let's say 1MB cutoff
			if ((file instanceof File && file.size() > 1024 * 1024) ||
				(file instanceof RaMixEntry && file.space() > 1024 * 1024)) {
				return Music
			}
			return Sound
		},
		'cps': { file -> Image },
		'pal': { file -> Palette },
		'pcx': { file -> Image },
		'shp': { file -> SpriteSheet },
		'sno': { file -> SpriteSheet },
		'tem': { file -> SpriteSheet },
		'v00': { file -> Sound },
		'v01': { file -> Sound },
		'v02': { file -> Sound },
		'v03': { file -> Sound },
		'vqa': { file -> Video },
		'wsa': { file -> Animation }
	]

	static final Map<Class, String> DECODER_TO_TYPE = [
		(AudFileDecoder): 'AUD sound file',
		(CpsFileDecoder): 'CPS image file',
		(PcxFileDecoder): 'PCX image file',
		(ShpFileDecoder): 'SHP sprite sheet file',
		(TmpFileRADecoder): 'Tilemap file (RA)',
		(TmpFileTDDecoder): 'Tilemap file (TD)',
		(VqaFileDecoder): 'VQA video file',
		(WsaFileDecoder): 'WSA animation file'
	]

	static final Map<String, String> FILE_EXTENSION_TO_TYPE = [
		'aud': 'AUD sound file',
		'cps': 'CPS image file',
		'mix': 'MIX archive file',
		'pal': 'PAL palette file',
		'pcx': 'PCX image file',
		'shp': 'SHP sprite sheet file',
		'sno': 'SNO tilemap file',
		'tem': 'TEM tilemap file',
		'v00': 'AUD sound file',
		'v01': 'AUD sound file',
		'v02': 'AUD sound file',
		'v03': 'AUD sound file',
		'vqa': 'VQA video file',
		'wsa': 'WSA animation file'
	]

	/**
	 * Return a class that can handle the current file, or {@code null} if
	 * there is no implementation for it.
	 */
	static Class getSupportedFileClass(File self) {

		if (self) {
			var fileExtension = self.name.substring(self.name.lastIndexOf('.') + 1)
			var closure = FILE_EXTENSION_TO_MEDIA_CLASS[fileExtension]
			if (closure) {
				return closure(self)
			}
		}
		return null
	}

	/**
	 * Return a class that can handle the current mix file entry, or {@code null}
	 * if there is no implementation for it.
	 */
	static Class getSupportedFileClass(MixData self) {

		if (self) {
			var fileExtension = self.name().substring(self.name().lastIndexOf('.') + 1)
			var closure = FILE_EXTENSION_TO_MEDIA_CLASS[fileExtension]
			if (closure) {
				return closure(self)
			}
		}
		return null
	}

	/**
	 * Return a class that can handle the current RA-MIXer database entry, or
	 * {@code null} if there is no implementation for it.
	 */
	static Class getSupportedFileClass(RaMixEntry self) {

		if (self) {
			var fileExtension = self.name().substring(self.name().lastIndexOf('.') + 1)
			var closure = FILE_EXTENSION_TO_MEDIA_CLASS[fileExtension]
			if (closure) {
				return closure(self)
			}
		}
		return null
	}

	/**
	 * Return the media class that can be created from the current decoder class.
	 */
	static Class getSupportedFileClass(Class self) {

		return self ? DECODER_TO_MEDIA_CLASS[self] : null
	}

	/**
	 * Return a supported file extension for the current decoder class.
	 */
	static String getSupportedFileExtension(Class self) {

		return self ? DECODER_TO_EXTENSION[self] : null
	}

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String getSupportedFileType(File self) {

		return self?.file ? FILE_EXTENSION_TO_TYPE[self.name.substring(self.name.lastIndexOf('.') + 1)] : null
	}

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String getSupportedFileType(MixData self) {

		return self ? FILE_EXTENSION_TO_TYPE[self.name().substring(self.name().lastIndexOf('.') + 1)] : null
	}

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String getSupportedFileType(RaMixEntry self) {

		return self ? FILE_EXTENSION_TO_TYPE[self.name().substring(self.name().lastIndexOf('.') + 1)] : null
	}

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String getSupportedFileType(Class self) {

		return self ? DECODER_TO_TYPE[self] : null
	}
}
