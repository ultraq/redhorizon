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

package nz.net.ultraq.redhorizon.filetypes

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import java.nio.ByteBuffer
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveTask

/**
 * A {@code ForkJoinTask} for applying a palette to a colour-indexed image.
 * 
 * @author Emanuel Rabina
 */
@CompileStatic
@TupleConstructor(defaults = false)
class ApplyPaletteTask extends RecursiveTask<ByteBuffer> {

	private static final int TASK_THRESHOLD = 16000 // 320x50, or quarter of an old resolution frame

	final ByteBuffer indexedData
	final Palette palette
//	final ByteBuffer colouredData

	/**
	 * Apply the palette to the indexed data, returning the result in a new
	 * buffer.
	 * 
	 * @return
	 */
	private ByteBuffer applyPalette() {

		def colouredData = ByteBuffer.allocateNative(indexedData.remaining() * palette.format.value)
		while (indexedData.hasRemaining()) {
			colouredData.put(palette[indexedData.get() & 0xff])
		}
		return colouredData.flip()
	}

	@Override
	protected ByteBuffer compute() {

		def jobSize = indexedData.remaining()
		if (jobSize > TASK_THRESHOLD) {
			List<ForkJoinTask<ByteBuffer>> tasks = []
			for (def i = 0; i < jobSize; i += TASK_THRESHOLD) {
				tasks << new ApplyPaletteTask(
					indexedData.duplicate().position(i).limit(Math.min(i + TASK_THRESHOLD, jobSize)),
					palette
				)
					.fork()
			}
			def colouredData = ByteBuffer.allocateNative(jobSize * palette.format.value)
			tasks.each { task ->
				colouredData.put(task.join())
			}
			return colouredData.flip()
		}
		return applyPalette()
	}
}
