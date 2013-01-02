
// ===================================
// Scanner's Java - Single player game
// ===================================

package redhorizon.engine;

/**
 * Abstract class for all gameplay types.  Contains a simple lifecycle to allow
 * control of the game in the implementing class.
 * <p>
 * The game engine first instantiates an implementation, then calls this class'
 * {@link #init()} method to allow the implementation to do any pre-loading of
 * resources or whatever other initialization tasks need handling.
 * <p>
 * After initialization, the class is then wrapped in a thread and that thread's
 * <tt>start()</tt> method is called.  For implementations, this means writing
 * the <tt>run()</tt> method as you would any other thread.
 * <p>
 * The engine then sits and waits for the thread to complete execution, which
 * means the implementing class must return when gameplay is complete.  When
 * this happens, the {@link #close()} method is called to allow the proper
 * shutdown and closing of any resources.
 * 
 * @author Emanuel Rabina
 */
public abstract class GameFlow implements Runnable {

	/**
	 * Sub-class-visible default constructor.
	 */
	protected GameFlow() {
	}

	/**
	 * Called by the game engine after gameplay has completed to allow the
	 * implementing class to release any resources.
	 */
	public abstract void close();

	/**
	 * Called by the game engine to allow the implementing class to do any
	 * loading/preloading before the game is started.
	 */
	public abstract void init();

	/**
	 * Called by this class when the game engine has started a new thread.  It
	 * is in this method that implementations will have their game-specific
	 * behaviour.
	 */
	protected abstract void play();

	/**
	 * Kicks-off the <tt>GameFlow</tt> implementation.  Wraps exceptions that
	 * occur during game play in a <tt>GameFlowException</tt>.
	 * 
	 * @throws GameFlowException Wrapper for exceptions that occur during
	 * 		   gameplay.
	 */
	public final void run() throws GameFlowException {

		try {
			// Game-specific initialization
			init();

			// Game-specific play
			play();

			// Game-specific shutdown
			close();
		}
		catch (Exception ex) {
			throw new GameFlowException(ex.getMessage(), ex);
		}
	}
}
