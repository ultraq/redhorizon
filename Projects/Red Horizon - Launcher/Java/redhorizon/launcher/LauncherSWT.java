
package redhorizon.launcher;

import redhorizon.launcher.Launcher;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * SWT implementation of the launcher.
 * 
 * @author Emanuel Rabina
 */
public class LauncherSWT extends Launcher {

	private final Display display;
	private final Shell shell;

	/**
	 * Constructor, creates a new launcher GUI.
	 */
	public LauncherSWT() {

		display = Display.getDefault();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void closeImpl() {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void openImpl() {
		// TODO Auto-generated method stub

	}
}
