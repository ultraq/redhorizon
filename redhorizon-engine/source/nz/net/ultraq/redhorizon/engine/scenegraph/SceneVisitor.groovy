/*
 * Copyright 2017, Emanuel Rabina (http://www.ultraq.net.nz/)
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
 * Interface for anything that wants to be able to traverse the scene graph.
 *
 * @author Emanuel Rabina
 */
@FunctionalInterface
interface SceneVisitor {

	/**
	 * Allow visiting any scene element.
	 *
	 * @param element
	 * @return
	 * {@code true} if the visit can continue to this node's children, {@code
	 * false} otherwise.
	 */
	boolean visit(Node element)
}
