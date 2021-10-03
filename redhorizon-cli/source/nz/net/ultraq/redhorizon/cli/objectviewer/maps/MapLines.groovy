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

package nz.net.ultraq.redhorizon.cli.objectviewer.maps

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.scenegraph.SceneElement

import org.joml.Vector2f

/**
 * Map overlays and lines to help with debugging maps.
 * 
 * @author Emanuel Rabina
 */
class MapLines implements GraphicsElement, SceneElement<MapLines> {

	private static final Vector2f X_AXIS_MIN = new Vector2f(-3600, 0)
	private static final Vector2f X_AXIS_MAX = new Vector2f(3600, 0)
	private static final Vector2f Y_AXIS_MIN = new Vector2f(0, -3600)
	private static final Vector2f Y_AXIS_MAX = new Vector2f(0, 3600)

	final MapRA map
	private Material axisLines
	private Material boundaryLines

	/**
	 * Constructor, set the bounds of this object for culling purposes.
	 * 
	 * @param map
	 */
	MapLines(MapRA map) {

		this.map = map
		this.bounds.set(X_AXIS_MIN.x, Y_AXIS_MIN.y, X_AXIS_MAX.x, Y_AXIS_MAX.y)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMesh(axisLines.mesh)
		renderer.deleteMesh(boundaryLines.mesh)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		axisLines = renderer.createMaterial(
			mesh: renderer.createLinesMesh(Colour.RED.withAlpha(0.5), X_AXIS_MIN, X_AXIS_MAX, Y_AXIS_MIN, Y_AXIS_MAX),
			transform: transform
		)
		boundaryLines = renderer.createMaterial(
			mesh: renderer.createLineLoopMesh(Colour.YELLOW.withAlpha(0.5), map.boundary as Vector2f[]),
			transform: transform
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(axisLines)
		renderer.drawMaterial(boundaryLines)
	}
}
