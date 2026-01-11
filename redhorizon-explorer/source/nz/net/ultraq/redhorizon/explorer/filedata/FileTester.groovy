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

package nz.net.ultraq.redhorizon.explorer.filedata

import nz.net.ultraq.redhorizon.audio.Music
import nz.net.ultraq.redhorizon.audio.Sound
import nz.net.ultraq.redhorizon.classic.filetypes.AudFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.CpsFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.FileTypeTest
import nz.net.ultraq.redhorizon.classic.filetypes.PcxFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileRADecoder
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileTDDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.VqaFileDecoder
import nz.net.ultraq.redhorizon.classic.filetypes.WsaFileDecoder
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.Video

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Attempt to determine which file decoder is most appropriate for a given file.
 *
 * <p>While the file extension is often useful, there are many instances of C&C
 * files either having a different extension, or an extension being ambiguous.
 *
 * @author Emanuel Rabina
 */
class FileTester {

	private static final Logger logger = LoggerFactory.getLogger(FileTester)
	private static final List<Class<? extends FileTypeTest>> decoderClasses = [
		AudFileDecoder, CpsFileDecoder, PcxFileDecoder, ShpFileDecoder, TmpFileRADecoder, TmpFileTDDecoder,
		VqaFileDecoder, WsaFileDecoder
	]
	private static final Map<Class, String> decoderToType = [
		(AudFileDecoder): 'AUD sound file',
		(CpsFileDecoder): 'CPS image file',
		(PcxFileDecoder): 'PCX image file',
		(ShpFileDecoder): 'SHP sprite sheet file',
		(TmpFileRADecoder): 'Tilemap file (RA)',
		(TmpFileTDDecoder): 'Tilemap file (TD)',
		(VqaFileDecoder): 'VQA video file',
		(WsaFileDecoder): 'WSA animation file'
	]

	/**
	 * Run the input stream through each of the registered decoders until a match,
	 * if any, is found.
	 */
	FileTesterResult test(String fileName, long fileSize, InputStream inputStream) {

		var fileExtension = fileName ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : null

		for (var decoderClass : decoderClasses) {
			var decoder = decoderClass.getConstructor().newInstance()
			if (!fileExtension || decoder.supportedFileExtensions.contains(fileExtension)) {
				var result = inputStream.markAndReset(512) { stream ->
					try {
						decoder.test(stream)
						logger.debug('Decoder check passed, using {}', decoderClass.simpleName)
						return new FileTesterResult(decoderClass, decoderToType[decoderClass],
							switch (decoderClass) {
								case AudFileDecoder -> fileSize > (1024 * 1024) ? Music : Sound
								case CpsFileDecoder, PcxFileDecoder -> Image
								case ShpFileDecoder, TmpFileRADecoder, TmpFileTDDecoder -> SpriteSheet
								case VqaFileDecoder -> Video
								case WsaFileDecoder -> Animation
								default -> null
							})
					}
					catch (AssertionError ignored) {
						return null
					}
				}
				if (result) {
					return result
				}
			}
		}
		return null
	}

	static record FileTesterResult(Class decoder, String type, Class mediaClass) {}
}
