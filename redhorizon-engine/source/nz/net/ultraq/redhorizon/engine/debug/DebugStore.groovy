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

import nz.net.ultraq.redhorizon.scenegraph.Node

/**
 * A node for storing debug information.  Holds some basic state that is
 * provided by Red Horizon, but is a good place for other games to attach their
 * own data nodes.
 *
 * @author Emanuel Rabina
 */
class DebugStore extends Node<DebugStore> {

	boolean showCollisionOutlines = false
	boolean showDebugOverlay = false
	boolean showGridLines = false
	boolean showLogPanel = false
	boolean showNodeList = false

	/**
	 * Set all of the debug flags to {@code false}/off.
	 */
	void disableAll() {

		showCollisionOutlines = false
		showDebugOverlay = false
		showGridLines = false
		showLogPanel = false
		showNodeList = false
	}

	/**
	 * Set all of the debug flags to {@code true}/on.
	 */
	void enableAll() {

		showCollisionOutlines = true
		showDebugOverlay = true
		showGridLines = true
		showLogPanel = true
		showNodeList = true
	}
}
