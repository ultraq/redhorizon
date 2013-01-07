/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.utilities;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class used to schedule one-time tasks.  Can be reset and re-used on those
 * same tasks more than once.
 * <p>
 * The <tt>Timer</tt> runs with a minimum resolution of 100ms between event
 * checks.
 * 
 * @author Emanuel Rabina
 */
public class Timer implements Runnable {

	// Global timer
	public static final Timer GLOBAL_TIMER = new Timer("Global timer");

	// Minimum number of milliseconds to sleep between checking for event changes
	private static final int RESOLUTION_DEFAULT = 100;

	// Animation parameters
	private final String name;
	private final int resolution;
	private final TreeMap<Integer,TimerTask> tasks = new TreeMap<>();

	private volatile boolean running;
	private volatile boolean paused;

	/**
	 * Constructor, assigns this timer a series of tasks with the default timer
	 * resolution.
	 * 
	 * @param name Timer name, also the name of the thread used to run the
	 * 			   timer.
	 */
	public Timer(String name) {

		this(name, RESOLUTION_DEFAULT);
	}

	/**
	 * Constructor, creates a new timer with all settings specified.
	 * 
	 * @param name		 Timer name, also the name of the thread used to run the
	 * 					 timer.
	 * @param resolution The minimum wait time before checking for events to
	 * 					 fire.  Must be greater than 0.
	 * @throws IllegalArgumentException If any of the arguments do not follow
	 * 		   the guidelines above.
	 */
	public Timer(String name, int resolution) throws IllegalArgumentException {

		if (resolution < 1) {
			throw new IllegalArgumentException("Resolution must be greater than 0.");
		}

		this.name       = name;
		this.resolution = resolution;
	}

	/**
	 * Adds a task to this timer.  Tasks can only be assigned while the timer is
	 * not running.  Tasks assigned while running will be ignored.
	 * 
	 * @param task Timer task for assignment.
	 * @param time The time, in milliseconds, after the timer starts at which
	 * 			   the task is to be run.
	 */
	public synchronized void addTask(TimerTask task, int time) {

		if (!running) {
			tasks.put(time, task);
		}
	}

	/**
	 * Halts the timer, allowing it to resume from the pause point afterwards.
	 */
	public synchronized void pause() {

		paused = true;
	}

	/**
	 * Causes the current thread to wait on the timer's monitor.
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
	 * Resumes the timer from the pause state.
	 */
	public synchronized void resume() {

		paused = false;
		notifyAll();
	}

	/**
	 * Executes the timer and checks for whether there is a timed task to call
	 * in the time that has passed.
	 */
	@Override
	public void run() {

		Thread.currentThread().setName("Timer - " + name);

		// Set of scheduled times in ascending order
		ExecutorService threadpool = Executors.newCachedThreadPool();
		final Queue<Integer> times = new LinkedList<>(tasks.keySet());

		final long starttime = System.currentTimeMillis();
		long runtime = 0;
		long pausetime = 0;

		// Run timer as long as there are tasks to execute
		while (running && times.size() > 0) {

			// Remove pauses from the execution time
			if (paused) {
				long pausestart = System.currentTimeMillis();
				pause0();
				pausetime += (System.currentTimeMillis() - pausestart);
			}

			// Check for tasks to execute
			while (times.size() > 0 && times.peek() <= runtime) {
				threadpool.execute(new Runnable() {
					@Override
					public void run() {
						tasks.get(times.remove()).begin();
					}
				});
			}

			// Wait
			try {
				Thread.sleep(resolution);
			}
			catch (InterruptedException ie) {
				running = false;
				break;
			}

			// Increment time elapsed
			runtime = System.currentTimeMillis() - starttime - pausetime;
		}

		threadpool.shutdown();
	}

	/**
	 * Starts this timer.  A timer will stop itself once all tasks added to this
	 * timer have been called.
	 * <p>
	 * Starting an already-started timer has no effect.
	 */
	public synchronized void start() {

		if (!running) {
			running = true;
			new Thread(this, name).start();
		}
	}

	/**
	 * Stops this timer.
	 * 
	 * <p>Stopping an already-stopped (or unstarted) timer has no effect.
	 */
	public synchronized void stop() {

		running = false;
		if (paused) {
			resume();
		}
	}
}
