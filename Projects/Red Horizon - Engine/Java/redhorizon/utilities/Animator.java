
// =================================
// Scanner's Java - Animator utility
// =================================

package redhorizon.utilities;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class used to schedule repeated tasks for a given length of time, or
 * infinitely if specified.  Can be reset and re-used on those same tasks more
 * than once.
 * <p>
 * The <tt>Animator</tt> runs with a minimum resolution of 20ms between event
 * calls.
 * 
 * @author Emanuel Rabina
 */
public class Animator implements Runnable {

	// Represents an infinitely-looping animation
	public static final int CYCLE_INFINITE = -1;

	// Minimum number of milliseconds to sleep between checking for event changes
	private static final int RESOLUTION_DEFAULT = 20;

	// Animation parameters
	private final String name;
	private final long duration;
	private final int cycles;
	private final int resolution;
	private final ArrayList<AnimatorTask> tasks = new ArrayList<AnimatorTask>();

	private volatile boolean running;
	private volatile boolean paused;

	/**
	 * Constructor, creates an <tt>Animator</tt> with the given settings and the
	 * default resolution.
	 * 
	 * @param name	   Task name, also the name of the thread used to perform
	 * 				   the animation.
	 * @param duration The number of milliseconds per cycle.  Must be greater
	 * 				   than 0.
	 * @param cycles   The number of cycles to perform (animator repeats).  Must
	 * 				   be greater than 0, or {@link #CYCLE_INFINITE} if this
	 * 				   animator is to repeat endlessly.
	 * @throws IllegalArgumentException If any of the arguments do not follow
	 * 		   the guidelines above.
	 */
	public Animator(String name, int duration, int cycles)
		throws IllegalArgumentException {

		this(name, duration, cycles, RESOLUTION_DEFAULT);
	}

	/**
	 * Constructor, specifies all settings for a new <tt>Animator<tt>.
	 * 
	 * @param name		 Task name, also the name of the thread used to perform
	 * 					 the animation.
	 * @param duration	 The number of milliseconds per cycle.  Must be greater
	 * 					 than 0.
	 * @param cycles	 The number of cycles to perform (animator repeats).
	 * 					 Must be greater than 0, or {@link #CYCLE_INFINITE} if
	 * 					 this animator is to repeat endlessly.
	 * @param resolution The minimum wait time between event notifications.
	 * 					 Must be greater than 0.
	 * @throws IllegalArgumentException If any of the arguments do not follow
	 * 		   the guidelines above.
	 */
	public Animator(String name, int duration, int cycles, int resolution)
		throws IllegalArgumentException {

		if (duration < 1) {
			throw new IllegalArgumentException("Duration must be greater than 0.");
		}
		if (cycles < 1 && cycles != CYCLE_INFINITE) {
			throw new IllegalArgumentException("Cycles must be greater than 0.");
		}
		if (resolution < 1) {
			throw new IllegalArgumentException("Resolution must be greater than 0.");
		}

		this.name       = name;
		this.duration   = duration;
		this.cycles     = cycles;
		this.resolution = resolution;
	}

	/**
	 * Adds a task to this animator.  Tasks can only be assigned while the
	 * animator is not running.  Tasks assigned while running will be ignored.
	 * 
	 * @param task Animator task for assignment.
	 */
	public synchronized void addTask(AnimatorTask task) {

		if (!running) {
			tasks.add(task);
		}
	}

	/**
	 * Halts the animator, allowing it to resume from the pause point
	 * afterwards.
	 */
	public synchronized void pause() {

		paused = true;
	}

	/**
	 * Causes the current thread to wait on the animator's monitor.
	 */
	private synchronized void pause0() {

		while (paused) {
			try {
				wait();
			}
			catch (InterruptedException ie) {
				paused = false;
			}
		}
	}

	/**
	 * Resumes the animator from the pause state.
	 */
	public synchronized void resume() {

		paused = false;
		notifyAll();
	}

	/**
	 * Executes the animator and calls the {@link AnimatorTask} at regular
	 * intervals, defined by the animator's resolution.
	 */
	public void run() {

		// Cached thread pool to run animation events
		ExecutorService threadpool = Executors.newCachedThreadPool();

		final long starttime = System.currentTimeMillis();
		long runtime = 0;
		long pausetime = 0;

		// Notify tasks of start
		for (final AnimatorTask task: tasks) {
			threadpool.execute(new Runnable() {
				public void run() {
					task.begin();
				}
			});
		}

		// Repeat cycles
		cycle: for (int cycle = 0; running && cycle < cycles; cycle++) {

			// Track time running
			runtime = System.currentTimeMillis() - starttime - pausetime;
			while (running) {

				// Remove pauses from the execution time
				if (paused) {
					long pausestart = System.currentTimeMillis();
					pause0();
					pausetime += (System.currentTimeMillis() - pausestart);
				}

				// Notify task handlers
				final float fraction = (float)runtime / (float)duration;
				for (final AnimatorTask task: tasks) {
					threadpool.execute(new Runnable() {
						public void run() {
							task.event(fraction);
						}
					});
				}

				// Wait
				try {
					Thread.sleep(resolution);
				}
				catch (InterruptedException ie) {
					running = false;
					break cycle;
				}

				// Increment time elapsed
				runtime = System.currentTimeMillis() - starttime - pausetime;
			}
		}

		// End animator
		if (running) {
			for (final AnimatorTask task: tasks) {
				threadpool.execute(new Runnable() {
					public void run() {
						task.end();
					}
				});
			}
			running = false;
		}

		threadpool.shutdown();
	}

	/**
	 * Starts this animator.  Starting an already-started animator has no
	 * effect.
	 */
	public synchronized void start() {

		if (!running) {
			running = true;
			new Thread(this, name).start();
		}
	}

	/**
	 * Stops this animator, bypassing the call to {@link AnimatorTask#end()}
	 * in the process.  Stopping an already-stopped (or unstarted) animator has
	 * no effect.
	 */
	public synchronized void stop() {

		running = false;
		if (paused) {
			resume();
		}
	}
}
