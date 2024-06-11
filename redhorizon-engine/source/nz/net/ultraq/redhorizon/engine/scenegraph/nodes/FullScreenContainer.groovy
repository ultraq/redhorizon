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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.primitives.Rectanglef

import java.util.concurrent.CompletableFuture

/**
 * A node used for making its children take up the whole screen.
 *
 * @author Emanuel Rabina
 */
class FullScreenContainer extends Node<FullScreenContainer> {

	static enum FillMode {
		STRETCH,
		ASPECT_RATIO
	}

	FillMode fillMode = FillMode.ASPECT_RATIO

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return CompletableFuture.runAsync { ->
			bounds
				.set(scene.window.renderResolution as Rectanglef)
				.center()

			// Update children to take up the full screen
			children.each { child ->
				child.accept { Node node ->
					node.bounds.center()
				}
				switch (fillMode) {
					case FillMode.ASPECT_RATIO -> {
						child.setScaleXY(bounds.calculateScaleToFit(child.bounds))
					}
					case FillMode.STRETCH -> {
						child.setScale(
							bounds.lengthX() / child.bounds.lengthX() as float,
							bounds.lengthY() / child.bounds.lengthY() as float
						)
					}
				}
			}
		}
	}
}
