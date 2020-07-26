/* 
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes.png

import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGBA

import java.nio.ByteBuffer
import javax.imageio.ImageIO

/**
 * Wrapper of the Java implementation of Portable Network Graphics (PNG) images.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('png')
class PngFile implements ImageFile {

	final int width
	final int height
	final ColourFormat format
	final ByteBuffer imageData
	final Palette palette

	// Save-to information
	private static final int COMBINED_WIDTH_MAX = 1000

	/**
	 * Constructor, creates a new PNG from the given input stream.
	 * 
	 * @param inputStream
	 */
	PngFile(InputStream inputStream) {

		def image = ImageIO.read(inputStream)

		width  = image.width
		height = image.height
		format = image.colorModel.numComponents == 4 ? FORMAT_RGBA : FORMAT_RGB

		imageData = ByteBuffer.allocateNative(width * height)
		def rgbArray = image.getRGB(0, 0, width, height, null, 0, width)
		rgbArray.each { pixel ->
			imageData.putInt(pixel)
		}
		imageData.rewind()
	}

	/**
	 * Constructor, creates a PNG file from an animation.  This just redirects
	 * to the {@link #PngFile(String, ImagesFile, String...)} constructor since
	 * the animation-specific parts don't affect the conversion.
	 * 
	 * @param name			The name of this file.
	 * @param animationfile File to source data from.
	 * @param params		Additional parameters: external palette (opt).
	 */
//	PngFile(String name, AnimationFile animationfile, String... params) {
//
//		this(name, (ImagesFile)animationfile, params)
//	}

	/**
	 * Constructor, allows the construction of a new PNG file from the given
	 * {@link ImagesFile} implementation, and the accompanying parameters.
	 * 
	 * @param name		 The name of this file.
	 * @param imagesfile File to source data from.
	 * @param params	 Additional parameters: external palette (opt).
	 */
//	PngFile(String name, ImagesFile imagesfile, String... params) {
//
//		super(name)
//
//		// Load palette from 0th parameter
//		if (imagesfile instanceof Paletted && params.length > 0) {
//			try (PalFile palfile = new PalFile("PNG palette", FileChannel.open(Paths.get(params[0])))) {
//				pngpalette = new PngPalette(palfile)
//			}
//		}
//
//		// Load palette from internal palette
//		else if (imagesfile instanceof PalettedInternal) {
//			pngpalette = new PngPalette(((PalettedInternal)imagesfile).getPalette())
//		}
//
//		// Unsupported conversion
//		else {
//			throw new IllegalArgumentException()
//		}
//
//		// Construct single image from the multiple images inside the file
//		int maximagewidth  = imagesfile.width()
//		int maximageheight = imagesfile.height()
//		int numimages = imagesfile instanceof WsaFile && ((WsaFile<?>)imagesfile).isLooping() ?
//				imagesfile.numImages() + 1 : imagesfile.numImages()
//		int numimageshor = ImageUtility.fitImagesAcross(imagesfile.width(), numimages, COMBINED_WIDTH_MAX)
//		int numimagesver = (int)Math.ceil(numimages / (double)numimageshor)
//
//		width  = maximagewidth * numimageshor
//		height = maximageheight * numimagesver
//		format = imagesfile.format()
//
//		int[] srcwidths  = new int[numimages]
//		int[] srcheights = new int[numimages]
//
//		ByteBuffer[] allimages = new ByteBuffer[imagesfile.numImages()]
//		try (ReadableByteChannel srcimagesbytes = imagesfile instanceof PalettedInternal ?
//				((PalettedInternal)imagesfile).getRawImageData() : imagesfile.getImagesData()) {
//			for (int i = 0 i < allimages.length i++) {
//
//				int imagewidth = imagesfile instanceof ShpFileDune2 ?
//						((ShpFileDune2)imagesfile).width(i) : maximagewidth
//				int imageheight = imagesfile instanceof ShpFileDune2 ?
//						((ShpFileDune2)imagesfile).height(i) : maximageheight
//
//				srcwidths[i]  = imagewidth
//				srcheights[i] = imageheight
//
//				// Create an image the same size as the source frame
//				ByteBuffer image = ByteBuffer.allocate(imagewidth * imageheight)
//				srcimagesbytes.read(image)
//				image.rewind()
//				allimages[i] = image
//			}
//		}
//		// TODO: Should be able to soften the auto-close without needing this
//		catch (IOException ex) {
//			throw new RuntimeException(ex)
//		}
//
//		pngimage = ImageUtility.combineImages(numimageshor, numimagesver,
//				srcwidths, srcheights, FORMAT_INDEXED, allimages)
//	}

	/**
	 * Returns some information on this PNG file.
	 * 
	 * @return PNG file info.
	 */
	@Override
	String toString() {

		return "PNG file, ${width}x${height}, ${format == FORMAT_RGBA ? '32' : '24'}-bit colour"
	}

	/**
	 * {@inheritDoc}
	 */
//	@Override
//	void write(GatheringByteChannel outputchannel) {
//
//		// Create paletted PNG
//		if (isIndexed()) {
//
//			// Build the IndexColorModel (Java palette), use alpha
//			byte[] reds   = new byte[256]
//			byte[] greens = new byte[256]
//			byte[] blues  = new byte[256]
//			byte[] alphas = new byte[256]
//
//			for (int i = 0 i < 256 i++) {
//				byte[] colour = pngpalette.getColour(i)
//				reds[i]   = colour[0]
//				greens[i] = colour[1]
//				blues[i]  = colour[2]
//				alphas[i] = format == FORMAT_RGBA && pngpalette.format() == FORMAT_RGBA ? colour[3] : 0
//			}
//			IndexColorModel colormodel = new IndexColorModel(8, 256, reds, greens, blues, alphas)
//
//			// Create new BufferedImage using the IndexColorModel, width, height
//			BufferedImage outputimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, colormodel)
//			byte[] outputimgdata = ((DataBufferByte)outputimg.getRaster().getDataBuffer()).getData()
//
//			// Write PNG image data into the BufferedImage
//			pngimage.get(outputimgdata).rewind()
//
//			// Write to file
//			ImageIO.write(outputimg, "PNG", Channels.newOutputStream(outputchannel))
//		}
//
//		// Create 32-bit PNG
//		else {
//			throw new UnsupportedFileException("Saving of non-paletted PNG files not supported")
//		}
//	}
}
