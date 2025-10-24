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

package nz.net.ultraq.redhorizon.classic.extensions

import nz.net.ultraq.redhorizon.classic.nodes.Layer
import nz.net.ultraq.redhorizon.scenegraph.Node

/**
 * Extensions to the {@link Node} class to work with the 2D nature of the
 * classic C&C games.
 *
 * @author Emanuel Rabina
 */
class NodeExtensions {

	/**
	 * Set which layer a node will belong to.
	 */
	static void setLayer(Node self, Layer layer) {

		self.setPosition(self.position.x(), self.position.y(), layer.value)
	}
}
