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

package nz.net.ultraq.redhorizon.explorer.ui

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsContext
import nz.net.ultraq.redhorizon.geometry.Dimension

import org.eclipse.swt.events.ControlAdapter
import org.eclipse.swt.events.ControlEvent
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Group
import org.lwjgl.opengl.swt.GLCanvas
import org.lwjgl.opengl.swt.GLData
import static org.eclipse.swt.SWT.*
import static org.lwjgl.opengl.swt.GLData.Profile

/**
 * An OpenGL context using SWTs OpenGL components.
 * 
 * @author Emanuel Rabina
 */
class SwtGLContext extends GraphicsContext {

	private final GLCanvas glCanvas

	final long window = 0
	Dimension framebufferSize
	Dimension windowSize
	Dimension renderResolution = new Dimension(640, 400)
	Dimension targetResolution

	/**
	 * Constructor, creates a new {@link GLCanvas} to render in the
	 * {@code composite} component.
	 * 
	 * @param composite
	 */
	SwtGLContext(Composite composite) {

		glCanvas = new GLCanvas(composite, NONE, new GLData().tap {
			profile = Profile.CORE
			majorVersion = 4
			minorVersion = 1
			swapInterval = 1
		}).tap {
			size = new Point(640, 400)
		}

		framebufferSize = new Dimension(glCanvas.size.x, glCanvas.size.y)
		targetResolution = new Dimension(glCanvas.size.x, glCanvas.size.y)

		composite.addControlListener(new ControlAdapter() {
			@Override
			void controlResized(ControlEvent event) {
				def newSize = ((Group)event.widget).size
				framebufferSize = new Dimension(newSize.x, newSize.y)
				targetResolution = new Dimension(newSize.x, newSize.y)
			}
		})
	}

	@Override
	void close() {
	}

	@Override
	void makeCurrent() {

		glCanvas.setCurrent()
	}

	@Override
	void releaseCurrent() {
	}

	@Override
	void swapBuffers() {

		glCanvas.swapBuffers()
	}

	@Override
	boolean windowShouldClose() {

		return glCanvas.disposed
	}

	@Override
	void windowShouldClose(boolean close) {

	}
}
