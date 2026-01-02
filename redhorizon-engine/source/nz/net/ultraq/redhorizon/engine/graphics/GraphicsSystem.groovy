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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.scenegraph.Scene

import groovy.transform.TupleConstructor

/**
 * Render graphical scene components, followed by the UI.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class GraphicsSystem extends System {

	final Window window
	final Framebuffer framebuffer
	final BasicShader shader
	private final List<GraphicsComponent> graphicsComponents = new ArrayList<>()
	private final List<ImGuiComponent> imguiComponents = new ArrayList<>()

	@Override
	void update(Scene scene, float delta) {

		graphicsComponents.clear()
		imguiComponents.clear()
		scene.traverse(Entity) { Entity entity ->
			entity.findComponentsByType(GraphicsComponent, graphicsComponents)
			entity.findComponentsByType(ImGuiComponent, imguiComponents)
		}

		window.useRenderPipeline()
			.scene { ->
				framebuffer.useFramebuffer { ->
					shader.useShader { shaderContext ->
						var camera = scene.findDescendent { it instanceof CameraEntity } as CameraEntity
						camera.render(shaderContext)
						graphicsComponents.each { component ->
							if (component.enabled) {
								component.render(shaderContext)
							}
						}
					}
				}
				return framebuffer
			}
			.ui(true) { context ->
				imguiComponents.each { component ->
					if (component.enabled) {
						component.render(context)
					}
				}
			}
			.end()
	}
}
