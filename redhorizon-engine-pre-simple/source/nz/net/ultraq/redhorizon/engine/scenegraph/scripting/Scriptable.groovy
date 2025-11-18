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

package nz.net.ultraq.redhorizon.engine.scenegraph.scripting

/**
 * A class that can have a script attached to customize its behaviour.
 *
 * @author Emanuel Rabina
 */
trait Scriptable<T extends Scriptable> {

	Script script

	/**
	 * Attach a script to this object to control its behaviour.  Currently only
	 * supports 1 script per object.
	 */
	T attachScript(Script script) {

		if (this.script) {
			throw new IllegalStateException('Script already attached to this node')
		}
		this.script = script.attachScriptable(this)
		return this
	}
}
