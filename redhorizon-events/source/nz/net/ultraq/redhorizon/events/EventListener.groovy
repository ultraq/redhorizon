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

package nz.net.ultraq.redhorizon.events

/**
 * Inspired by the DOM, an event listener is a function which is applied to an
 * {@link EventTarget} and invoked when that target emits the event being
 * listened for.
 *
 * @author Emanuel Rabina
 */
@FunctionalInterface
interface EventListener<E extends Event> {

	/**
	 * The action for handling the event for which the listener was registered.
	 *
	 * @param event
	 */
	void handleEvent(E event)
}
