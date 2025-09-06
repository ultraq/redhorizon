/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics

import java.nio.ByteBuffer
import javax.imageio.ImageIO

/**
 * An image decoder for PNG files.  This implementation is just a wrapper for
 * the PNG decoding provided by the `javax.imageio` package.
 *
 * @author Emanuel Rabina
 */
class PngImageDecoder implements ImageDecoder {

	final String[] supportedFileExtensions = ['png']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		var bufferedImage = ImageIO.read(inputStream)
		var width = bufferedImage.width
		var height = bufferedImage.height
		var colourChannels = bufferedImage.colorModel.numComponents

		return new DecodeSummary(width, height, colourChannels,
			bufferedImage.getRGB(0, 0, width, height, null, 0, width)
				.inject(ByteBuffer.allocateNative(width * height * colourChannels)) { ByteBuffer acc, pixel ->
					var red = (byte)(pixel >> 16)
					var green = (byte)(pixel >> 8)
					var blue = (byte)(pixel)
					var alpha = (byte)(pixel >> 24)
					acc.put(red).put(green).put(blue).put(alpha)
				}
				.flip()
				.flipVertical(width, height, colourChannels))
	}
}
