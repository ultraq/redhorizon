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
 * Inspired by the DOM, an event target is a class that can generate events
 * which can be listened for by the appropriate event listeners.
 * 
 * @author Emanuel Rabina
 */
trait EventTarget {

	private final Map<Class<? extends Event>, List<EventListener>> eventListeners = [:]

	/**
	 * Register an event listener on this event target.  When the event is fired
	 * by the target, then the listener will be invoked with that event.
	 * 
	 * @param event
	 * @param eventListener
	 * @return This object.
	 */
	public <E extends Event> EventTarget on(Class<E> event, EventListener<E> eventListener) {

		def listenersForEvent = eventListeners.getOrCreate(event) { ->
			return new ArrayList<EventListener>()
		}
		listenersForEvent << eventListener
		return this
	}

	/**
	 * Fire the event, invoking all listeners registered for that event.
	 * 
	 * @param event
	 * @return This object.
	 */
	public <E extends Event> EventTarget trigger(E event) {

		eventListeners[(Class<? extends Event>)event.class]?.each { listener ->
			listener.handleEvent(event)
		}
		return this
	}
}
