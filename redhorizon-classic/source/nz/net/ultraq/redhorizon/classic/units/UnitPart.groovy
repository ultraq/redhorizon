/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.units

import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement

import groovy.transform.TupleConstructor

/**
 * Base type for any separate component of a unit.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
abstract class UnitPart implements GraphicsElement {

	final Unit unit
	final int width
	final int height

	/**
	 * Returns the name of this type of part.
	 *
	 * @return
	 */
	abstract String getType()
}
