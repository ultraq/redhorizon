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

package nz.net.ultraq.redhorizon.utilities.objectviewer.maps

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Vector2f

import groovy.transform.TupleConstructor

/**
 * Map overlays and lines to help with debugging maps.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(includes = ['map'])
class MapLines implements GraphicsElement, SelfVisitable {

	private static final Vector2f X_AXIS_MIN = new Vector2f(-3600, 0)
	private static final Vector2f X_AXIS_MAX = new Vector2f(3600, 0)
	private static final Vector2f Y_AXIS_MIN = new Vector2f(0, -3600)
	private static final Vector2f Y_AXIS_MAX = new Vector2f(0, 3600)

	final MapRA map
	private Material axisLines
	private Material boundaryLines

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMesh(axisLines.mesh)
		renderer.deleteMesh(boundaryLines.mesh)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		axisLines = renderer.createMaterial(
			renderer.createLinesMesh(Colour.RED.withAlpha(0.5),
				X_AXIS_MIN,
				X_AXIS_MAX,
				Y_AXIS_MIN,
				Y_AXIS_MAX),
			null
		)
		boundaryLines = renderer.createMaterial(
			renderer.createLineLoopMesh(Colour.YELLOW.withAlpha(0.5), map.boundary.asPoints()),
			null
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(axisLines)
		renderer.drawMaterial(boundaryLines)
	}
}
