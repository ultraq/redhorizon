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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer

/**
 * A object that contains ImGui content to render and takes up window space.
 *
 * @author Emanuel Rabina
 */
interface ImGuiChrome {

	/**
	 * Is the current item in focus?
	 */
	boolean isFocused()

	/**
	 * Is the current item being hovered over?  (Mouse inputs only)
	 */
	boolean isHovered()

	/**
	 * Draw the ImGui content.
	 */
	void render(int dockspaceId, Framebuffer sceneFramebufferResult)
}
