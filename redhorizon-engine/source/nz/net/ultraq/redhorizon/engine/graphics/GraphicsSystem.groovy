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

import nz.net.ultraq.groovy.profilingextensions.LoggingStrategy
import nz.net.ultraq.groovy.profilingextensions.Profiler
import nz.net.ultraq.groovy.profilingextensions.TimedLoggingStrategy
import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.GraphicsNode
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule
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
	private final LoggingStrategy loggingStrategy = new TimedLoggingStrategy(1f)
	private final List<Long> sceneExecutionTimes = []
	private final List<Long> postProcessingExecutionTimes = []
	private final List<Long> uiExecutionTimes = []

	@Override
	void update(Scene scene, float delta) {

		graphicsNodes.clear()
		scene.findAll(GraphicsNode, graphicsNodes)
		imguiModules.clear()
		scene.findAll(ImGuiModule, imguiModules)

		// TODO: Create an allocation-free method of grouping objects
		var groupedNodes = graphicsNodes.groupBy { it.shaderClass }

		window.useRenderPipeline()
			.scene { ->
				return trackTime('GraphicsSystem::scene') { ->
					return framebuffer.useFramebuffer { ->
						shaders.each { shader ->
							shader.useShader { shaderContext ->
								var camera = scene.find(Camera)
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
				return trackTime('GraphicsSystem::postProcessing') { ->
					return postProcessingStage ? postProcessingStage(sceneBuffer) : null as Framebuffer
				}
			}
			.ui(true) { context ->
				trackTime('GraphicsSystem::ui') { ->
					imguiModules.each { component ->
						if (component.enabled) {
							component.render(context)
						}
					}
				}
			}
			.end()

		if (loggingStrategy.shouldLog()) {
			logger.debug(Profiler.marker, 'S: {}ms, PP: {}ms, UI: {}ms',
				sprintf('%.2f', getTimes('GraphicsSystem::scene', sceneExecutionTimes).average()),
				sprintf('%.2f', getTimes('GraphicsSystem::postProcessing', postProcessingExecutionTimes).average()),
				sprintf('%.2f', getTimes('GraphicsSystem::ui', uiExecutionTimes).average()))
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
