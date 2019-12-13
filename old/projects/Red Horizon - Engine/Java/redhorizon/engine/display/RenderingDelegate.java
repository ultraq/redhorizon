/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.engine.display;

/**
 * Interface for classes that wish to handle rendering events from the game
 * window canvas.
 * 
 * @author Emanuel Rabina
 */
public interface RenderingDelegate {

	/**
	 * Notification that the display area has been closed.
	 */
	public void displayClosed();

	/**
	 * Notification that there is now a visible rendering space being actively
	 * updated.
	 */
	public void displayInit();

	/**
	 * Notification that a new rendering cycle has started.
	 */
	public void displayRendering();
}
