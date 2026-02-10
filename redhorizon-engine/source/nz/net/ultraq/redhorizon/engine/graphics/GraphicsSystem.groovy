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

import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.GraphicsNode
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * Render graphical scene components, followed by the UI.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class GraphicsSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsSystem)

	final Window window
	final Framebuffer framebuffer
	final Shader<? extends SceneShaderContext>[] shaders
	private final List<GraphicsNode> graphicsNodes = new ArrayList<>()
	private final List<ImGuiModule> imguiModules = new ArrayList<>()
	private Closure<Framebuffer> postProcessingStage

	@Override
	void update(Scene scene, float delta) {

		average('Update', 1f, logger) { ->
			graphicsNodes.clear()
			imguiModules.clear()
			scene.traverse { Node node ->
				if (node instanceof GraphicsNode) {
					graphicsNodes << node
				}
				else if (node instanceof ImGuiModule) {
					imguiModules << node
				}
				return true
			}

			// TODO: Create an allocation-free method of grouping objects
			var groupedNodes = graphicsNodes.groupBy { it.shaderClass }

			window.useRenderPipeline()
				.scene { ->
					return average('Scene', 1f, logger) { ->
						return framebuffer.useFramebuffer { ->
							shaders.each { shader ->
								shader.useShader { shaderContext ->
									var camera = scene.findByType(Camera)
									if (camera) {
										camera.render(shaderContext)
									}
									groupedNodes[shader.class]?.each { graphics ->
										if (graphics.enabled) {
											graphics.render(shaderContext)
										}
									}
								}
							}
						}
					}
				}
				.postProcessing { sceneBuffer ->
					return average('Post-processing', 1f, logger) { ->
						return postProcessingStage ? postProcessingStage(sceneBuffer) : null as Framebuffer
					}
				}
				.ui(true) { context ->
					average('UI', 1f, logger) { ->
						imguiModules.each { component ->
							if (component.enabled) {
								component.render(context)
							}
						}
					}
				}
				.end()
		}
	}

	/**
	 * Configure the post-processing stage of the render pipeline.
	 */
	GraphicsSystem withPostProcessing(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.graphics.Framebuffer') Closure<Framebuffer> closure) {

		postProcessingStage = closure
		return this
	}
}
