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

import groovy.transform.TupleConstructor
import java.nio.ByteBuffer
import java.util.concurrent.RecursiveTask

/**
 * A {@code ForkJoinTask} for applying a palette to a colour-indexed image.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ApplyPaletteTask extends RecursiveTask<ByteBuffer> {

	private static final int TASK_THRESHOLD = 32000 // 320x100, or half an old resolution frame

	final ByteBuffer indexedData
	final Palette palette
	final IntRange range

	/**
	 * Apply the palette to the indexed data, returning the result in a new
	 * buffer.
	 * 
	 * @return
	 */
	private ByteBuffer applyPalette() {

		def dest = ByteBuffer.allocateNative(range.size() * palette.format.value)
		range.each { index ->
			dest.put(palette[indexedData.get(index) & 0xff])
		}
		return dest.flip()
	}

	@Override
	protected ByteBuffer compute() {

		def jobSize = range.size()
		if (jobSize > TASK_THRESHOLD) {
			def tasks = []
			for (def i = 0; i < jobSize; i += TASK_THRESHOLD) {
				tasks << new ApplyPaletteTask(indexedData, palette, i..<(Math.min(i + TASK_THRESHOLD, jobSize)))
					.fork()
			}
			def colouredBuffer = ByteBuffer.allocateNative(jobSize * palette.format.value)
			tasks.each { task ->
				colouredBuffer.put(task.join())
			}
			return colouredBuffer.flip()
		}
		return applyPalette()
	}
}
