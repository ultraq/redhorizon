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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.events.Event

import groovy.transform.MapConstructor

/**
 * Event for log lines that should be captured in the debug overlay.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor
class ImGuiLogEvent extends Event {

	final String message

	/**
	 * If present, then {@link #message} will be shown with the other persistent
	 * debug lines in the overlay.
	 */
	final String persistentKey
}
