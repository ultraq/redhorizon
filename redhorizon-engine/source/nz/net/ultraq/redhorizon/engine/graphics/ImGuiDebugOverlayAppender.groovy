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

package nz.net.ultraq.redhorizon.engine.graphics

import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.encoder.Encoder

/**
 * A custom logback appender made for moving logged events to the debug overlay
 * created using ImGui.
 * 
 * @author Emanuel Rabina
 */
class ImGuiDebugOverlayAppender<E> extends UnsynchronizedAppenderBase<E> {

	Encoder<E> encoder

	@Override
	protected void append(E eventObject) {

		def encodedEvent = encoder.encode(eventObject)
		ImGuiRenderer.instance?.addDebugLine(new String(encodedEvent))
	}
}
