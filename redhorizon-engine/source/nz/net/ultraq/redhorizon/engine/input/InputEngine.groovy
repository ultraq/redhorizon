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

package nz.net.ultraq.redhorizon.engine.input

import nz.net.ultraq.redhorizon.engine.Engine

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * The input subsystem which queues input events from some source to trigger
 * registered input handlers.
 * <p>
 * This is a very light wrapper around the existing event system, but made so
 * that events are run in a thread separate from the rendering one so as not to
 * block it.
 * 
 * @author Emanuel Rabina
 */
class InputEngine extends Engine {

	private static final Logger logger = LoggerFactory.getLogger(InputEngine)
	private static final int TARGET_RENDER_TIME_MS = 10

	private final Queue<InputEvent> inputQueue = new ConcurrentLinkedQueue<>()

	/**
	 * Constructor, set the source for input events that will end up on the input
	 * queue and actioned at set intervals.
	 * 
	 * @param inputSource
	 */
	InputEngine(InputSource inputSource) {

		super(TARGET_RENDER_TIME_MS)
		inputSource.on(InputEvent) { event ->
			inputQueue << event
		}
	}

	/**
	 * Start the input engine loop, working through queued input events and
	 * finding their respective input handlers to action them.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Input Engine'
		logger.debug('Starting input engine')

		logger.debug('Input engine in render loop...')
		renderLoop { ->
			inputQueue.size().times { i ->
				trigger(inputQueue.poll())
			}
		}
	}
}
