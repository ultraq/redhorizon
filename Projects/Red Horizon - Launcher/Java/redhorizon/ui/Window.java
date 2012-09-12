
package redhorizon.ui;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
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
	 * Creates a basic fill layout with the following settings:
	 * <ul>
	 *   <li>Margin Width:  10</li>
	 *   <li>Margin Height: 10</li>
	 *   <li>Spacing:       10</li>
	 * </ul>
	 * 
	 * @return A new fill layout based on the above values.
	 */
	protected static FillLayout createFillLayout() {

		return createFillLayout(10, 10, 10);
	}

	/**
	 * Creates a new fill layout with the specified values.
	 * 
	 * @param marginwidth
	 * @param marginheight
	 * @param spacing
	 * 
	 * @return A new fill layout based on the above values.
	 */
	protected static FillLayout createFillLayout(int marginwidth, int marginheight, int spacing) {

		FillLayout filllayout = new FillLayout();
		filllayout.marginWidth  = marginwidth;
		filllayout.marginHeight = marginheight;
		filllayout.spacing      = spacing;

		return filllayout;
	}

	/**
	 * Creates a new single-item no-margin layout using the grid layout.  That
	 * is, a layout with the following:
	 * <ul>
	 *   <li>Columns: 1</li>
	 *   <li>Equal Width: yes</li>
	 *   <li>Margin Top:    10</li>
	 *   <li>Margin Right:  10</li>
	 *   <li>Margin Bottom: 10</li>
	 *   <li>Margin Left:   10</li>
	 *   <li>Vertical Spacing:   10</li>
	 *   <li>Horizontal Spacing: 10</li>
	 * </ul>
	 * 
	 * @return A new grid layout based on the above values.
	 */
	protected static GridLayout createGridLayout() {

		return createGridLayout(1, true, 10, 10, 10, 10, 10, 10);
	}

	/**
	 * Creates a new grid layout with the specified values.
	 * 
	 * @param numcolumns   Number of columns in this layout.
	 * @param equalspace   Whether or not the columns should be of the same width.
	 * @param margintop	   Top margin.
	 * @param marginright  Right margin.
	 * @param marginbottom Bottom margin.
	 * @param marginleft   Left margin.
	 * @param horspacing   Horizontal spacing between widgets.
	 * @param verspacing   Verticle spacing between widgets.
	 * @return A grid layout using the specified values.
	 */
	protected static GridLayout createGridLayout(int numcolumns, boolean equalspace,
		int margintop, int marginright, int marginbottom, int marginleft,
		int horspacing, int verspacing) {

		GridLayout gridlayout = new GridLayout(numcolumns, equalspace);
		gridlayout.marginWidth  = 0;
		gridlayout.marginHeight = 0;
		gridlayout.marginTop    = margintop;
		gridlayout.marginLeft   = marginleft;
		gridlayout.marginRight  = marginright;
		gridlayout.marginBottom = marginbottom;
		gridlayout.horizontalSpacing = horspacing;
		gridlayout.verticalSpacing   = verspacing;

		return gridlayout;
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
