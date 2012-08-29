
package redhorizon.launcher;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Base class for all window types.
 * 
 * @author Emanuel Rabina
 */
public abstract class Window {

	protected final Display display;
	protected final Shell shell;

	/**
	 * Constructor, set any SWT hints for the window shell that is created.
	 * 
	 * @param swthints
	 */
	protected Window(int swthints) {

		display = Display.getDefault();
		shell = new Shell(display, swthints);
	}

	/**
	 * Close the window.
	 */
	public void close() {

		display.syncExec(new Runnable() {
			@Override
			public void run() {
				shell.dispose();
			}
		});
	}

	/**
	 * Opens the window.  This method will then block until the window is
	 * closed, either by an action in the window itself, or another thread
	 * calling the {@link #close()} method on this window.
	 */
	public void open() {

		pack();
		shell.open();

		// Wait until closed
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * Initialize the bounds (position and size) of the window.
	 */
	protected void pack() {

		shell.pack();
	}
}
