
// ===================================================
// Scanner's AspectJ - OpenGL error-checking mechanism
// ===================================================

package redhorizon.debug;

import redhorizon.engine.AudioEngine;

import net.java.games.joal.AL;
import net.java.games.joal.ALC;
import net.java.games.joal.ALCcontext;
import net.java.games.joal.ALCdevice;
import net.java.games.joal.ALException;
import static net.java.games.joal.AL.*;

import java.lang.reflect.Field;

/**
 * This aspect queries the error state of OpenAL after each OpenAL call.<br>
 * <br>
 * Much like OpenGL, OpenAL errors are only known if explicitly asked for.  The
 * query method is similar to OpenGL's, except for the name: alGetError().<br>
 * <br>
 * Note: this aspect is only active if the Debug parameter of the RedHorizon.ini
 * file is set to 'true'.
 * 
 * @author Emanuel Rabina
 */
public privileged aspect DebugAL {

	private static final boolean debug = Boolean.getBoolean("debug");

	/**
	 * Queries the error code to see if the previous OpenAL call caused an
	 * error.  Occurs after every call to a al*(..) method.
	 * 
	 * @throws ALException For whenever the previous OpenAL function caused some
	 * 					   sort of error.
	 */
	after() throws ALException: if (debug) &&
		call(public * AL.al*(..)) && !within(redhorizon.debug.Debug*) {

		AL al = (AL)thisJoinPoint.getTarget();
		int errorid = al.alGetError();
		String errorname = "(unknown error code)";

		// Get the error, if any
		if (errorid != AL_NO_ERROR) {

			// Use reflection to match the error ID to the error name
			for (Field field: AL.class.getFields()) {
				if (field.getType() == Integer.TYPE && errorid == field.getInt(al)) {
					errorname = field.getName();
					break;
				}
			}
			throw new ALException("JOAL error - " + errorname);
		}
	}

	/**
	 * Checks if the OpenAL device was opened successfully.
	 * 
	 * @param device The recently opened OpenAL device.
	 * @throws ALException If there were problems opening the device.
	 */
	after() returning(ALCdevice device) throws ALException: if (debug) &&
		call(public ALCdevice ALC.alcOpenDevice(String)) {

		if (device == null) {
			throw new ALException("Unable to open an OpenAL sound device.");
		}
	}

	/**
	 * Checks if the OpenAL device was closed successfully.
	 * 
	 * @param success Whether or not the operation succeeded.
	 * @throws ALException If there were problems closing the device.
	 */
	after() returning(boolean success) throws ALException: if (debug) &&
		call(public boolean ALC.alcCloseDevice(ALCdevice)) {

		if (!success) {
			throw new ALException("Unable to close the OpenAL sound device");
		}
	}

	/**
	 * Check if OpenAL context creation was successful.
	 * 
	 * @param context The recently created OpenAL context.
	 * @throws ALException If there were problems creating the context.
	 */
	after() returning(ALCcontext context) throws ALException: if (debug) &&
		call(public ALCcontext ALC.alcCreateContext(ALCdevice, ..)) {

		if (context == null) {
			throw new ALException("Unable to create an OpenAL context.");
		}
	}

	/**
	 * Checks if was able to make the OpenAL context current.
	 * 
	 * @param success Whether or not the operation succeeded.
	 */
	after() returning(boolean success) throws ALException: if (debug) &&
		call(public boolean ALC.alcMakeContextCurrent(ALCcontext)) &&
		withincode(private void AudioEngine.ALContextManager.makeCurrentContext()) {

		if (!success) {
			throw new ALException("Unable to make the OpenAL context current.");
		}
	}

	/**
	 * Checks if was able to release the OpenAL context current.
	 * 
	 * @param success Whether or not the operation succeeded.
	 */
	after() returning(boolean success) throws ALException: if (debug) &&
		call(public boolean ALC.alcMakeContextCurrent(ALCcontext)) &&
		withincode(private void AudioEngine.ALContextManager.releaseCurrentContext()) {

		if (!success) {
			throw new ALException("Unable to release the OpenAL context current.");
		}
	}
}
