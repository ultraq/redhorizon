/*
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer

/**
 * Interface for elements rendered by the graphics hardware.
 *
 * @author Emanuel Rabina
 */
interface GraphicsElement {

	/**
	 * Build a render command that can be used for later.
	 * <p>
	 * Implementations should ensure all mutable data is copied for use in the
	 * command as a defensive measure against changes by other threads in the
	 * space between now and running the command.
	 */
	RenderCommand renderCommand()

	/**
	 * A render instruction that can be executed later.
	 */
	@FunctionalInterface
	static interface RenderCommand {

		void render(GraphicsRenderer renderer)
	}
}
