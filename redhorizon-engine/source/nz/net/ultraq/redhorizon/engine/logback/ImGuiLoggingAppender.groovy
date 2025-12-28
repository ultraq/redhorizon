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

package nz.net.ultraq.redhorizon.engine.logback

import nz.net.ultraq.eventhorizon.EventTarget

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.encoder.Encoder

/**
 * A custom logback appender made for moving logged events to the debug overlay
 * created using ImGui.
 *
 * @author Emanuel Rabina
 */
class ImGuiLoggingAppender extends UnsynchronizedAppenderBase<ILoggingEvent> implements EventTarget<ImGuiLoggingAppender> {

	static ImGuiLoggingAppender instance

	Encoder<ImGuiLogEvent> encoder

	/**
	 * Constructor, saves this instance to the singleton value so it can be
	 * referenced by the overlay.
	 */
	ImGuiLoggingAppender() {

		instance = this
	}

	@Override
	protected void append(ILoggingEvent eventObject) {

		var message = new String(encoder.encode(eventObject))
		if (eventObject.message.contains('average time')) {
			trigger(new ImGuiLogEvent(message, eventObject.loggerName + eventObject.argumentArray[0]))
		}
		else {
			trigger(new ImGuiLogEvent(message))
		}
	}
}
