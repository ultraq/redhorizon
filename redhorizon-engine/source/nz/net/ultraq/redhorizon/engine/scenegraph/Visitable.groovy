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

package nz.net.ultraq.redhorizon.engine.scenegraph

/**
 * Elements in a scene which are visitable.
 * <p>
 * The behaviour of a visitable element is slightly different from standard
 * iteration in that the visitor can specify if it wishes to visit each of a
 * node's children or not, based on the return value from the visit.
 *
 * @author Emanuel Rabina
 */
interface Visitable {

	/**
	 * Accept any scene visitor.
	 */
	void accept(SceneVisitor visitor)
}
