/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.scenegraph;

/**
 * Interface for game objects that are selectable from the game world via the
 * user interface, and as such can response to user interface events.
 * 
 * @author Emanuel Rabina
 */
public interface Selectable {

	/**
	 * Notification that this object has been deselected by the user.
	 */
	public void deselect();

	/**
	 * Notification that this object has been selected by the user.
	 */
	public void select();
}
