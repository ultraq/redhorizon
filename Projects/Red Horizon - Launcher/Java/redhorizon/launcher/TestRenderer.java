/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.launcher;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL2.*;

/**
 * Does some basic OpenGL stuff to show that rendering is working.  A basic
 * substitute for the graphics engine.
 * 
 * @author Emanuel Rabina
 */
public class TestRenderer implements DisplayRenderer {

	private GL2 gl;
	private GLDrawableFactory factory;
	private GLContext glcontext;

	private int rot = 0;

	/**
	 * Constructor, set up the OpenGL renderer.
	 */
	public TestRenderer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void displayRendering() {

		// Clear color and depth buffer
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(.3f, .5f, .8f, 1.0f);

		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -10.0f);
		float frot = rot;
		gl.glRotatef(0.15f * rot, 2.0f * frot, 10.0f * frot, 1.0f);
		gl.glRotatef(0.3f * rot, 3.0f * frot, 1.0f * frot, 1.0f);
		rot++;
		gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		gl.glColor3f(0.9f, 0.9f, 0.9f);
		drawTorus(1, 1.9f + ((float) Math.sin((0.004f * frot))), 15, 15);
	}

	/**
	 * Draw a torus.
	 * 
	 * @param r
	 * @param R
	 * @param nsides
	 * @param rings
	 */
	private void drawTorus(float r, float R, int nsides, int rings) {

		float ringDelta = 2.0f * (float) Math.PI / rings;
		float sideDelta = 2.0f * (float) Math.PI / nsides;
		float theta = 0.0f, cosTheta = 1.0f, sinTheta = 0.0f;
		for (int i = rings - 1; i >= 0; i--) {
			float theta1 = theta + ringDelta;
			float cosTheta1 = (float) Math.cos(theta1);
			float sinTheta1 = (float) Math.sin(theta1);
			gl.glBegin(GL_QUAD_STRIP);
			float phi = 0.0f;
			for (int j = nsides; j >= 0; j--) {
				phi += sideDelta;
				float cosPhi = (float) Math.cos(phi);
				float sinPhi = (float) Math.sin(phi);
				float dist = R + r * cosPhi;
				gl.glNormal3f(cosTheta1 * cosPhi, -sinTheta1 * cosPhi, sinPhi);
				gl.glVertex3f(cosTheta1 * dist, -sinTheta1 * dist, r * sinPhi);
				gl.glNormal3f(cosTheta * cosPhi, -sinTheta * cosPhi, sinPhi);
				gl.glVertex3f(cosTheta * dist, -sinTheta * dist, r * sinPhi);
			}
			gl.glEnd();
			theta = theta1;
			cosTheta = cosTheta1;
			sinTheta = sinTheta1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void displayShutdown() {

		glcontext.release();
		glcontext.destroy();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void displayStartup() {

		GLProfile profile = GLProfile.get(GLProfile.GL2);
		factory = GLDrawableFactory.getFactory(profile);
		glcontext = factory.createExternalGLContext();
		glcontext.makeCurrent();
		gl = glcontext.getGL().getGL2();

		// Set up the camera
		gl.glViewport(0, 0, 800, 600);
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();

//		gl.glOrtho(-400, 400, -300, 300, 0, 100);
		GLU glu = new GLU();
		glu.gluPerspective(45.0f, 800f / 600f, 0.5f, 400.0f);

		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
	}
}
