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

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.MeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayoutPart
import nz.net.ultraq.redhorizon.engine.graphics.opengl.PrimitivesShader
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.Vector2f
import org.joml.primitives.Rectanglef

/**
 * A node used for making its children take up the whole screen.
 *
 * @author Emanuel Rabina
 */
class FullScreenContainer extends Node<FullScreenContainer> implements GraphicsElement {

	static enum FillMode {
		STRETCH,
		ASPECT_RATIO
	}

	FillMode fillMode = FillMode.ASPECT_RATIO

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	@Override
	void init(GraphicsRenderer renderer) {
	}

	@Override
	void onSceneAdded(Scene scene) {

		bounds
			.set(scene.window.renderResolution as Rectanglef)
			.center()

		// Update children to take up the full screen
		children.each { child ->
			switch (fillMode) {
				case FillMode.ASPECT_RATIO -> {
					child.transform.scaleXY(bounds.calculateScaleToFit(child.bounds))
				}
				case FillMode.STRETCH -> {
					child.transform.scaleXY(
						bounds.lengthX() / child.bounds.lengthX() as float,
						bounds.lengthY() / child.bounds.lengthY() as float
					)
				}
			}
		}

		addChild(new Outline())
		super.onSceneAdded(scene)
	}

	@Override
	void render(GraphicsRenderer renderer) {
	}

	// TODO: Make this a primitives node
	static class Outline extends Node<Outline> implements GraphicsElement {

		private Mesh mesh
		private Shader shader

		@Override
		void delete(GraphicsRenderer renderer) {
		}

		@Override
		void init(GraphicsRenderer renderer) {
		}

		@Override
		void onSceneAdded(Scene scene) {

			bounds.set(
				parent.bounds.minX + 5 as float,
				parent.bounds.minY + 5 as float,
				parent.bounds.maxX - 5 as float,
				parent.bounds.maxY - 5 as float
			)
			mesh = scene
				.requestCreateOrGet(new MeshRequest(
					MeshType.LINE_LOOP,
					new VertexBufferLayout(VertexBufferLayoutPart.COLOUR, VertexBufferLayoutPart.POSITION),
					Colour.GREEN,
					bounds as Vector2f[]
				))
				.get()
			shader = scene
				.requestCreateOrGet(new ShaderRequest(PrimitivesShader.NAME))
				.get()

			super.onSceneAdded(scene)
		}

		@Override
		void onSceneRemoved(Scene scene) {

			scene.requestDelete(mesh)
		}

		@Override
		void render(GraphicsRenderer renderer) {

			if (!mesh || !shader) {
				return
			}

			var globalTransform = getGlobalTransform()
			renderer.draw(mesh, globalTransform, shader)
		}
	}
}
