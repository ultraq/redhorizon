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

package nz.net.ultraq.redhorizon.runtime.logback

import nz.net.ultraq.groovy.profilingextensions.ProfilingExtensions

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

/**
 * A logging filter that drops any performance/profiling-related messages.
 *
 * @author Emanuel Rabina
 */
class ExcludePerformanceStatsFilter extends Filter<ILoggingEvent> {

	@Override
	FilterReply decide(ILoggingEvent event) {

		return event.markerList?.contains(ProfilingExtensions.profilingMarker) ?
			FilterReply.DENY :
			FilterReply.NEUTRAL
	}
}
