/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.geometry

import java.util.function.Function

/**
 * A class to hold a single calculated value that changes only when any of its
 * configured dependencies (which are also calculated values) change.
 *
 * @author Emanuel Rabina
 */
abstract class CalculatedValue<T> {

	protected final List<CalculatedValue> dependencies
	protected final Function<List<CalculatedValue>, T> calculate
	protected boolean changed = true
	private T lastValue

	CalculatedValue(List<CalculatedValue> dependencies, Function<List<CalculatedValue>, T> calculate) {

		this.dependencies = dependencies
		this.calculate = calculate
	}

	/**
	 * Return the calculated value, either being updated through changed
	 * dependencies, or the last returned value if dependencies haven't changed.
	 */
	T get() {

		if (changed) {
			lastValue = calculate.apply(dependencies)
			changed = false
		}
		return lastValue
	}

	/**
	 * Perform modification to the value within the closure, marking it as having
	 * been modified.
	 */
	abstract void modify(Closure closure)
}
