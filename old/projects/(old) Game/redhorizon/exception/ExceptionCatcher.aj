
// ===================================================
// Scanner's AspectJ - Sets a thread exception inputhandler
// ===================================================

package redhorizon.exception;

import redhorizon.engine.GameEngine;
import redhorizon.engine.GameEngineException;
import redhorizon.engine.display.DisplayCallback;

/**
 * Exceptions that occur in separate threads usually get lost before causing the
 * thread to die.  To catch any exceptions in another thread, an exception
 * inputhandler can be set.  In the case of Red Horizon, the threads of the
 * game's sub-engines will be handled by the main game engine, whereas the main
 * game engine will be handled by the main thread of execution.<br>
 * <br>
 * This aspect will create inter-type declarations for these threads that
 * implement the <code>Thread.UncaughtExceptionHandler</code> interface.  It
 * also sends lost exceptions to the main thread of execution so that an error
 * message can be displayed.
 * 
 * @author Emanuel Rabina
 */
public privileged aspect ExceptionCatcher {

	// Track engine execution and exceptions
	private static GameEngineException GameEngine.exception;
	private static boolean running = true;

	/**
	 * If errors have occurred, bypass loading of the game.
	 * 
	 * @param engine The main game engine
	 */
	void around(GameEngine engine):
		call(private void GameEngine.loadGame()) && this(engine) {

		if (running) {
			proceed(engine);
		}
	}

	/**
	 * Catches and re-routes exceptions caught on {@link GameWindow}
	 * implementations, as exceptions will get lost on the window's own drawing
	 * loop.
	 * 
	 * TODO: Make errors here stop the game engine
	 */
	void around():
		call(public void DisplayCallback.displayRendering()) {

		try {
			proceed();
		}
		catch (Exception ex) {
			System.out.println("Rendering exception");
			ex.printStackTrace();
			Thread thread = Thread.currentThread();
			thread.getThreadGroup().uncaughtException(thread, ex);
		}
	}

	/**
	 * Inter-type declaration, sets the {@link GameEngine} class'
	 * <code>uncaughtException(Thread, Throwable)</code> so that it becomes the
	 * <code>UncaughtExceptionHandler</code> for all engine threads created by
	 * the game engine, including the game engine itself.
	 * 
	 * @param t The thread that has stopped.
	 * @param e The exception that has occurred.
	 */
	public synchronized void GameEngine.uncaughtException(Thread t, Throwable e) {

		subengineinit.release();

		// Bypass if already run
		if (!running) {
			return;
		}
		running = false;

		// Stop sub-engines
		audio.stop();
		graphics.stop();
		input.stop();

		// Set the exception
		exception = new GameEngineException("An exception has occurred in thread " + t.getName(), e);
	}

	/**
	 * Additional exception handling, check that the inter-type variable
	 * <code>exception</code> is not null.  If it is, an exception has occurred
	 * and it should be thrown so that an error message can be displayed.
	 */
	after():
		call(public void Thread.join()) && withincode(public static void GameEngine.start()) {

		if (GameEngine.exception != null) {
			throw GameEngine.exception;
		}
	}
}
