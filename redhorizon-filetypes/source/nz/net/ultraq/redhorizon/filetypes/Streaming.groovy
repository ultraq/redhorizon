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
 * expose that streaming data through {@link Worker} classes that can be started
 * and listened to from the by the consuming thread.
 * 
 * @author Emanuel Rabina
 */
interface Streaming {

	/**
	 * Returns a worker that can be used for streaming file data.  Workers can be
	 * listened to for events that contain data in useful portions that are usable
	 * by some kind of consumer.
	 * 
	 * @return
	 *   A worker that can be executed as its own thread for generating the
	 *   streaming data.
	 */
	Worker getStreamingDataWorker()
}
