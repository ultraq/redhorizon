/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics.pipeline

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Window

/**
 * A render pass to upscale a frame to the target resolution while maintaining
 * sharpness.
 *
 * @author Emanuel Rabina
 */
class SharpUpscalingPostProcessingRenderPass extends PostProcessingRenderPass {

	SharpUpscalingPostProcessingRenderPass(Mesh fullScreenMesh, GraphicsRenderer renderer, Window window) {

		super(fullScreenMesh, renderer, window, renderer.createShader(new SharpUpscalingShader()), true)
	}
}
