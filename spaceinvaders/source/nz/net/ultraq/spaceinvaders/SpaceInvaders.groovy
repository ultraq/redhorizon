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

package nz.net.ultraq.spaceinvaders

import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Spec
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT

import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Space Invaders! 👾
 * <p>
 * A Space Invaders clone, built using the Red Horizon engine as a way to test
 * if the engine can be used to build a simple game.
 * 
 * @author Emanuel Rabina
 */
@Command(
	name = "spaceinvaders",
	header = [
		'',
		'Space Invaders! 👾',
		'=================',
		''
	],
	mixinStandardHelpOptions = true,
	version = '${sys:redhorizon.version}'
)
class SpaceInvaders extends Application implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(SpaceInvaders)

	@Spec
	CommandSpec commandSpec

	@Option(names = ['--full-screen'], description = 'Run in fullscreen mode')
	boolean fullScreen

	/**
	 * Begin the game.
	 * 
	 * @return
	 */
	@Override
	Integer call() {

		Thread.currentThread().name = 'Application'
		logger.info('Space Invaders! 👾 {}', commandSpec.version()[0] ?: '(development)')

		logger.debug('Starting game systems...');
		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			useGameClock(executorService) { gameClock ->

				def graphicsConfig = new GraphicsConfiguration(
					fullScreen: fullScreen
				)
				logger.debug('Using graphics configuration of: {}', graphicsConfig)

				useGraphicsEngine(executorService, graphicsConfig) { graphicsEngine ->
					useInputEngine(executorService, graphicsEngine) { inputEngine ->

						logger.debug('Building level scene...')
						def levelScene = new Scene()
						levelScene << new PlayerShip()

						graphicsEngine.scene = levelScene

						// Key event handler
						inputEngine.on(KeyEvent) { event ->
							if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
								switch (event.key) {
								case GLFW_KEY_ESCAPE:
									inputEngine.stop()
									graphicsEngine.stop()
									break
								}
							}
						}
					}
				}
			}
		}
		return 0
	}

	/**
	 * Bootstrap the application using Picocli.
	 * 
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(new CommandLine(new SpaceInvaders()).execute(args))
	}
}
