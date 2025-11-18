/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine

import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.audio.AudioRendererEvent
import nz.net.ultraq.redhorizon.engine.audio.BufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.audio.BufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.audio.SourceCreatedEvent
import nz.net.ultraq.redhorizon.engine.audio.SourceDeletedEvent
import nz.net.ultraq.redhorizon.engine.game.GameLogicSystem
import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.UniformBufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.UniformBufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.NodeAddedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.NodeRemovedEvent

import java.util.concurrent.atomic.AtomicInteger

/**
 * A record of several stats that we want to track for debugging purposes.
 *
 * @author Emanuel Rabina
 */
@Singleton
class EngineStats {

	final AtomicInteger sceneObjects = new AtomicInteger()

	final AtomicInteger drawCalls = new AtomicInteger()
	final AtomicInteger activeFramebuffers = new AtomicInteger()
	final AtomicInteger activeMeshes = new AtomicInteger()
	final AtomicInteger activeTextures = new AtomicInteger()
	final AtomicInteger activeUniformBuffers = new AtomicInteger()

	final AtomicInteger activeSources = new AtomicInteger()
	final AtomicInteger activeBuffers = new AtomicInteger()

	/**
	 * Add the audio renderer to get stats on audio sources, buffers, etc.
	 */
	EngineStats attachAudioRenderer(AudioRenderer audioRenderer) {

		audioRenderer.on(AudioRendererEvent) { event ->
			switch (event) {
				case BufferCreatedEvent -> activeBuffers.incrementAndGet()
				case BufferDeletedEvent -> activeBuffers.decrementAndGet()
				case SourceCreatedEvent -> activeSources.incrementAndGet()
				case SourceDeletedEvent -> activeSources.decrementAndGet()
			}
		}
		return this
	}

	/**
	 * Add the game logic system to get stats on the game world.
	 */
	EngineStats attachGameLogicSystem(GameLogicSystem gameLogicSystem) {

		gameLogicSystem.scene.on(NodeAddedEvent) { event ->
			sceneObjects.incrementAndGet()
		}
		gameLogicSystem.scene.on(NodeRemovedEvent) { event ->
			sceneObjects.decrementAndGet()
		}
		return this
	}

	/**
	 * Add the graphics renderer to get stats on draws, textures, etc.
	 */
	EngineStats attachGraphicsRenderer(GraphicsRenderer graphicsRenderer) {

		graphicsRenderer.on(GraphicsRendererEvent) { event ->
			switch (event) {
				case DrawEvent -> drawCalls.incrementAndGet()
				case FramebufferCreatedEvent -> activeFramebuffers.incrementAndGet()
				case FramebufferDeletedEvent -> activeFramebuffers.decrementAndGet()
				case MeshCreatedEvent -> activeMeshes.incrementAndGet()
				case MeshDeletedEvent -> activeMeshes.decrementAndGet()
				case TextureCreatedEvent -> activeTextures.incrementAndGet()
				case TextureDeletedEvent -> activeTextures.decrementAndGet()
				case UniformBufferCreatedEvent -> activeUniformBuffers.incrementAndGet()
				case UniformBufferDeletedEvent -> activeUniformBuffers.decrementAndGet()
			}
		}
		return this
	}
}
