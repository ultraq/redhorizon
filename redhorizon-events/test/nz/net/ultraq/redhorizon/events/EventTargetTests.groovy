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

package nz.net.ultraq.redhorizon.events

import spock.lang.Specification

/**
 * Tests for the event target trait which includes the event registration and
 * triggering mechanisms.
 *
 * @author Emanuel Rabina
 */
class EventTargetTests extends Specification {

	private class TestEventTarget implements EventTarget {}

	private class TestEvent extends Event {}

	private class TestSubclassEvent extends TestEvent {}

	def target = new TestEventTarget()

	def 'Handler invoked for exact event class matches'() {
		given:
			def listener = Mock(EventListener)
			target.on(TestEvent, listener)
			def event = new TestEvent()
		when:
			target.trigger(event)
			Thread.sleep(200) // Event trigger is async
		then:
			1 * listener.handleEvent(event)
	}

	def 'Handler invoked for subclass event matches'() {
		given:
			def listener = Mock(EventListener)
			target.on(TestEvent, listener)
			def event = new TestSubclassEvent()
		when:
			target.trigger(event)
			Thread.sleep(200) // Event trigger is async
		then:
			1 * listener.handleEvent(event)
	}

	def 'Exceptions in handlers do not prevent execution of further handlers'() {
		given:
			def target = new TestEventTarget()
			def event = new TestEvent()
			def listener2 = Mock(EventListener)
			target.on(TestEvent) { e ->
				throw new Exception()
			}
			target.on(TestEvent, listener2)
		when:
			target.trigger(event)
			Thread.sleep(200) // Event trigger is async
		then:
			notThrown(Exception)
			1 * listener2.handleEvent(event)
	}
}
