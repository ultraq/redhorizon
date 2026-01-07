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

package nz.net.ultraq.redhorizon.engine.extensions

import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ProfilingLoggingOverlayModule
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.CursorTrackingOverlayModule
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay

/**
 * Extensions for common use of the {@link DebugOverlay}.
 *
 * @author Emanuel Rabina
 */
class DebugOverlayExtensions {

	/**
	 * Simplifies the addition of the {@link CursorTrackingOverlayModule} to work
	 * with the camera entity.
	 */
	static DebugOverlay withCursorTracking(DebugOverlay self, Window window, CameraEntity cameraEntity) {

		return self << new CursorTrackingOverlayModule(window, cameraEntity.camera, cameraEntity.transform)
	}

	/**
	 * Simplifies the addition of the {@link ProfilingLoggingOverlayModule}.
	 */
	static DebugOverlay withProfilingLogging(DebugOverlay self) {

		return self << new ProfilingLoggingOverlayModule()
	}
}
