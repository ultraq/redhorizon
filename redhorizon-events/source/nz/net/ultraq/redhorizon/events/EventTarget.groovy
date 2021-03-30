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

import java.util.concurrent.ExecutorService

/**
 * Inspired by the DOM, an event target is a class that can generate events
 * which can be listened for by the appropriate event listeners.
 * 
 * @author Emanuel Rabina
 */
trait EventTarget {

	private final List<EventAndListenerPair> eventListeners = []

	/**
	 * Register an event listener on this event target.  When the event is fired
	 * by the target, then the listener will be invoked with that event.
	 * 
	 * @param eventClass
	 * @param eventListener
	 */
	public <E extends Event> void on(Class<E> eventClass, EventListener<E> eventListener) {

		eventListeners << new EventAndListenerPair(eventClass, eventListener)
	}

	/**
	 * Re-fire events on this class through the given event target, effectively
	 * forwarding events.
	 * 
	 * @param eventClass
	 * @param newTarget
	 */
	public <E extends Event> void relay(Class<E> eventClass, EventTarget newTarget) {

		this.on(eventClass) { event ->
			newTarget.trigger(event)
		}
	}

	/**
	 * Fire the event, invoking all listeners registered for that event.  If the
	 * optional {@code executorService} is provided, then listeners are notified
	 * using the executor's {@code execute} method.
	 * 
	 * @param event
	 * @param executorService
	 * @return This object.
	 */
	public <E extends Event> void trigger(E event, ExecutorService executorService = null) {

		def notifyListeners = { ->
			eventListeners.each { pair ->
				if (pair.event.isInstance(event)) {
					pair.listener.handleEvent(event)
				}
			}
		}

		if (executorService) {
			executorService.execute(notifyListeners)
		}
		else {
			notifyListeners()
		}
	}
}
