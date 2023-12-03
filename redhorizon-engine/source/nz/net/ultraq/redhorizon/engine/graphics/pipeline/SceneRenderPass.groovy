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

import nz.net.ultraq.redhorizon.engine.graphics.Camera
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.scenegraph.ElementAddedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.ElementRemovedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneElement

import org.joml.FrustumIntersection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.PackageScope
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The render pass for drawing the scene to a framebuffer at the rendering
 * resolution.
 *
 * @author Emanuel Rabina
 */
@PackageScope
class SceneRenderPass implements RenderPass<Boolean> {

	private static final Logger logger = LoggerFactory.getLogger(SceneRenderPass)

	private final Scene scene
	private final Camera camera
	private final Set<GraphicsElement> initialized = new HashSet<>()
	final Framebuffer framebuffer

	// For object lifecycles
	private final CopyOnWriteArrayList<SceneElement> addedElements = new CopyOnWriteArrayList<>()
	private final CopyOnWriteArrayList<SceneElement> removedElements = new CopyOnWriteArrayList<>()

	// For object culling
	private final List<GraphicsElement> visibleElements = []
	private final AtomicBoolean sceneChanged = new AtomicBoolean(true)

	SceneRenderPass(Scene scene, Camera camera, Framebuffer framebuffer) {

		this.scene = scene
		this.scene.on(ElementAddedEvent) { event ->
			addedElements << event.element
			sceneChanged.set(true)
		}
		this.scene.on(ElementRemovedEvent) { event ->
			removedElements << event.element
			sceneChanged.set(true)
		}

		this.camera = camera
		this.framebuffer = framebuffer
		this.enabled = true
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		scene.accept { element ->
			if (element instanceof GraphicsElement) {
				element.delete(renderer)
			}
		}
	}

	@Override
	void render(GraphicsRenderer renderer, Boolean cameraMoved) {

		// Initialize or delete objects which have been added/removed to/from the scene
		if (addedElements) {
			def elementsToInit = new ArrayList<SceneElement>(addedElements)
			elementsToInit.each { elementToInit ->
				elementToInit.accept { element ->
					if (element instanceof GraphicsElement) {
						element.init(renderer)
						initialized << element
					}
				}
			}
			addedElements.removeAll(elementsToInit)
		}
		if (removedElements) {
			def elementsToDelete = new ArrayList<SceneElement>(removedElements)
			elementsToDelete.each { elementToDelete ->
				elementToDelete.accept { element ->
					if (element instanceof GraphicsElement) {
						element.delete(renderer)
					}
				}
			}
			removedElements.removeAll(elementsToDelete)
		}

		// Reduce the list of renderable items to those just visible in the scene
		averageNanos('objectCulling', 1f, logger) { ->
			if (sceneChanged.get() || cameraMoved) {
				visibleElements.clear()
				def frustumIntersection = new FrustumIntersection(camera.projection * camera.view)
				scene.accept { element ->
					if (element instanceof GraphicsElement && frustumIntersection.testPlaneXY(element.bounds) &&
						initialized.contains(element)) {
						visibleElements << element
					}
				}
				sceneChanged.compareAndSet(true, false)
			}
		}

		visibleElements.each { element ->
			element.render(renderer)
		}
	}
}
