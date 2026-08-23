/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.debug

import nz.net.ultraq.groovy.profilingextensions.LoggingStrategy
import nz.net.ultraq.groovy.profilingextensions.Profiler
import nz.net.ultraq.groovy.profilingextensions.TimedLoggingStrategy
import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor

/**
 * Manage the drawing of debug information for debugging.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class DebugSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(DebugSystem)

	final DebugCollisionOutlineSystem collisionOutlineSystem
	final DebugMovementArrowsSystem movementArrowsSystem
	private final LoggingStrategy loggingStrategy = new TimedLoggingStrategy(1f)
	private final List<Long> collisionExecutionTimes = []
	private final List<Long> movementExecutionTimes = []

	@Override
	void update(Scene scene, float delta) {

		trackTime('DebugSystem::collisionOutline') { ->
			collisionOutlineSystem.update(scene, delta)
		}
		trackTime('DebugSystem::movementArrows') { ->
			movementArrowsSystem.update(scene, delta)
		}

		if (loggingStrategy.shouldLog()) {
			logger.atDebug()
				.addMarker(Profiler.PROFILER_MARKER)
				.addMarker(Profiler.AVERAGE_MARKER)
				.setMessage('C: {}ms, M: {}ms')
				.addArgument(() -> sprintf('%.2f', getTimes('DebugSystem::collisionOutline', collisionExecutionTimes).average()))
				.addArgument(() -> sprintf('%.2f', getTimes('DebugSystem::movementArrows', movementExecutionTimes).average()))
				.log()
		}
	}
}
