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

package nz.net.ultraq.redhorizon.explorer.controllers

import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.explorer.objects.GlobalPalette
import nz.net.ultraq.redhorizon.input.KeyBinding

import static org.lwjgl.glfw.GLFW.GLFW_KEY_P

/**
 * Controller for cycling the global palette on press of the P key.
 *
 * @author Emanuel Rabina
 */
class CyclePaletteController extends Script {

	@Override
	void init() {

		input.addInputBinding(new KeyBinding(GLFW_KEY_P, true, { ->
			node.scene.find(GlobalPalette).cyclePalette()
		}))
	}
}
