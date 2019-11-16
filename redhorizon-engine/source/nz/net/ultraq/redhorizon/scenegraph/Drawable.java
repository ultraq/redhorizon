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

import redhorizon.engine.graphics.GraphicsObject;

/**
 * Interface to define methods for objects that can be drawn onto the
 * screen.  Primarily, it's control over whether to have these objects drawn at
 * the next rendering pass.
 * 
 * @author Emanuel Rabina
 */
public interface Drawable extends GraphicsObject {

	/**
	 * Requests that the object be drawn from the next rendering pass onwards.
	 */
	public void draw();

	/**
	 * Requests that the object stop being drawn from the next rendering pass
	 * onwards.
	 */
	public void erase();

	/**
	 * Queries whether this item is being drawn.
	 * 
	 * @return <tt>true</tt> if it is to be drawn, <tt>false</tt> otherwise.
	 */
	public boolean isDrawing();
}
