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

package nz.net.ultraq.redhorizon.engine.graphics

/**
 * An abstraction of the main window's menu bar.  Shared on the scene so that
 * other things can make modifications to the menu.
 *
 * @author Emanuel Rabina
 */
abstract class GameMenu {

	final List<MenuItem> additionalOptionsItems = []

	/**
	 * Add a menu item that'll appear in the Options menu.
	 */
	void addOptionsItem(MenuItem menuItem) {

		additionalOptionsItems << menuItem
	}

	/**
	 * An additional menu item and the behaviour behind it.
	 */
	static abstract class MenuItem {

		/**
		 * Draw this menu item.
		 */
		abstract void render()
	}
}
