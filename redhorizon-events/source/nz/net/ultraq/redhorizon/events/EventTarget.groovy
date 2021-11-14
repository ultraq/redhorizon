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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Inspired by the DOM, an event target is a class that can generate events
 * which can be listened for by the appropriate event listeners.
 * 
 * @author Emanuel Rabina
 */
trait EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(EventTarget)
	private static final ExecutorService executorService = Executors.newCachedThreadPool()

	private final List<Tuple3<Class<? extends Event>, EventListener<? extends Event>, Boolean>> eventListeners =
		new CopyOnWriteArrayList<>()

	/**
	 * Register an event listener on this event target.  When the event is
	 * triggered by the target, then the listener will be invoked with that event
	 * on a separate thread from what triggered the event.
	 * 
	 * @param eventClass
	 * @param eventListener
	 */
	public <E extends Event> void on(Class<E> eventClass, EventListener<E> eventListener) {

		on(eventClass, false, eventListener)
	}

	/**
	 * Register an event listener on this event target, with the configured
	 * threading behaviour.  When the event is triggered by the target, then the
	 * listener will be invoked with that event, either on a separate thread from
	 * what triggered the event or the same thread.
	 * 
	 * @param eventClass
	 * @param eventListener
	 * @param useTriggeringThread
	 */
	public <E extends Event> void on(Class<E> eventClass, boolean useTriggeringThread, EventListener<E> eventListener) {

		eventListeners << new Tuple3<>(eventClass, eventListener, useTriggeringThread)
	}

	/**
	 * Re-fire events on this class through the given event target, effectively
	 * forwarding events.
	 * 
	 * @param eventClass
	 * @param newTarget
	 */
	public <E extends Event> void relay(Class<E> eventClass, EventTarget newTarget) {

		on(eventClass) { event ->
			newTarget.trigger(event)
		}
	}

	/**
	 * Fire the event, invoking all listeners registered for that event.
	 * 
	 * @param event
	 * @return This object.
	 */
	public <E extends Event> void trigger(E event) {

		eventListeners.each { tuple ->
			def (eventClass, listener, useTriggeringThread) = tuple
			if (eventClass.isInstance(event)) {
				try {
					if (useTriggeringThread) {
						listener.handleEvent(event)
					}
					else {
						executorService.execute { ->
							listener.handleEvent(event)
						}
					}
				}
				catch (Exception ex) {
					logger.error("An error occurred while processing ${event.class.simpleName} events on ${this.class.simpleName}", ex)
				}
			}
		}
	}
}
