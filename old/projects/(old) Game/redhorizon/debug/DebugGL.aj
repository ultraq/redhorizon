
// ===================================================
// Scanner's AspectJ - OpenGL error-checking mechanism
// ===================================================

package redhorizon.debug;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;
import static javax.media.opengl.GL.*;

import java.lang.reflect.Field;

/**
 * This aspect queries the error state of OpenGL after each OpenGL call.<br>
 * <br>
 * In OpenGL, errors are only ever made known if explicitly asked for.  The way
 * to query the error state is to call glGetError() after any OpenGL call to see
 * if the previous function caused an error.  As you can imagine, putting that
 * line after every GL call will be messy.  That's where this aspect comes
 * in.<br>
 * <br>
 * Note: this aspect is only active if the Debug parameter of the RedHorizon.ini
 * file is set to 'true'.
 * 
 * @author Emanuel Rabina
 */
public aspect DebugGL {

	private static final boolean debug = Boolean.getBoolean("debug");
	private static boolean immediatemode;

	/**
	 * Queries the error code to see if the previous OpenGL call caused an
	 * error.  Occurs after every call to a gl*(..) method, except for those
	 * within a <code>glBegin()</code>/<code>glEnd()</code> block.
	 * 
	 * @throws GLException For whenever the previous OpenGL function caused some
	 * 					   sort of error.
	 */
	after() throws GLException: if (debug) &&
		call(public * GL.gl*(..)) && !within(redhorizon.debug.Debug*) {

		// Don't query the error state when in immediate mode
		if (immediatemode) {
			return;
		}

		GL gl = (GL)thisJoinPoint.getTarget();
		int errorid = gl.glGetError();
		String errorname = "(unknown error code)";

		// Get the error, if any
		if (errorid != GL_NO_ERROR) {

			// Use reflection to match the error ID to the error name
			for (Field field: GL.class.getFields()) {
				if (errorid == field.getInt(gl)) {
					errorname = field.getName();
					break;
				}
			}
			throw new GLException("JOGL error - " + errorname);
		}
	}

	/**
	 * Set the immediate mode flag when a call to <code>glBegin()</code> is
	 * made.
	 */
	before(): if (debug) &&
		call(* GL.glBegin(..)) {

		immediatemode = true;
	}

	/**
	 * Drop the immediate mode flag when a call to <code>glEnd()</code> has been
	 * made.
	 */
	before(): if (debug) &&
		call(* GL.glEnd()) {

		immediatemode = false;
	}

	/**
	 * Checks to see if the attempt to make the OpenGL context current was
	 * successful.
	 * 
	 * @param success Value indicating the result of the operation.
	 * @throws GLException If the OpenGL context couldn't be made current.
	 */
	after() returning(int success) throws GLException: if (debug) &&
		call(public int GLContext.makeCurrent()) {

		if (success == GLContext.CONTEXT_NOT_CURRENT) {
			throw new GLException("Unable to make the OpenGL context current.");
		}
	}
}
