/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics.imgui

import nz.net.ultraq.redhorizon.graphics.Window

import imgui.ImFont
import org.joml.primitives.Rectanglei

/**
 * ImGui-related data to pass along to ImGui components when rendering.
 *
 * @author Emanuel Rabina
 */
interface ImGuiContext {

	/**
	 * Get the current ImGui dockspace ID.
	 */
	int getDockspaceId()

	/**
	 * Get the default font.
	 */
	ImFont getDefaultFont()

	/**
	 * Get the default monospace font.
	 */
	ImFont getMonospaceFont()

	/**
	 * Return coordinates for the area in which usable UI can be rendered.  This
	 * differs slightly from {@link Window#getViewport()} in that it returns
	 * values that can be used as layout coordinates for UI components.
	 */
	Rectanglei getUiArea()

	/**
	 * Get the factor by which UI content should be scale to account for the
	 * user's display.
	 */
	float getUiScale()
}
