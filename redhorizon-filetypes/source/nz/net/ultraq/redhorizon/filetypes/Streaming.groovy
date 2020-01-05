/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

/**
 * Certain files are better read by streaming their data out rather than
 * obtaining it all in one go, usually for the purpose of saving memory that
 * will be freed-up anyway.  This interface provides the ability for files to
 * expose that streaming data through {@link Worker} classes that can be
 * controlled by the consuming thread.
 * 
 * @author Emanuel Rabina
 */
interface Streaming {

	// TODO: This seems very much like events.  Maybe I should extract the event
	//       system in the engine package so it can be used here?

	/**
	 * Returns a worker that can be used for streaming file data.  Closures can
	 * then be attached to the worker for handling the data as it comes.  The
	 * parameters to that closure will always be {@code String} describing the
	 * kind of data, and the {@code ByteBuffer} of said data.
	 * 
	 * @return A worker that can be executed as its own thread for generating the
	 *   streaming data.
	 */
	Worker getStreamingDataWorker()
}
