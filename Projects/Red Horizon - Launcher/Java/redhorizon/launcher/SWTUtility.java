
package redhorizon.launcher;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * SWT utility methods.
 * 
 * @author Emanuel Rabina
 */
public class SWTUtility {

	/**
	 * Private default constructor.
	 */
	private SWTUtility() {
	}

	/**
	 * Center a shell on the screen.
	 * 
	 * @param shell
	 */
	public static void centerShell(Shell shell) {

		Rectangle resolution = shell.getDisplay().getPrimaryMonitor().getBounds();
		Rectangle window = shell.getBounds();
		int fromleft = (resolution.width >> 1)  - (window.width >> 1);
		int fromtop  = (resolution.height >> 1) - (window.height >> 1);
		shell.setBounds(new Rectangle(fromleft, fromtop, window.width, window.height));		
	}

	/**
	 * Creates a new single-item no-margin layout using the grid layout.  That
	 * is, a layout with the following:
	 * <ul>
	 *   <li>Columns: 1</li>
	 *   <li>Equal Width: yes</li>
	 *   <li>Margin Top:    0</li>
	 *   <li>Margin Right:  0</li>
	 *   <li>Margin Bottom: 0</li>
	 *   <li>Margin Left:   0</li>
	 *   <li>Vertical Spacing:   0</li>
	 *   <li>Horizontal Spacing: 0</li>
	 * </ul>
	 * 
	 * @return A new grid layout based on the above values.
	 */
	public static GridLayout createLayout() {

		return createLayout(1, true, 0, 0, 0, 0, 0, 0);
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
	public static GridLayout createLayout(int numcolumns, boolean equalspace,
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
}
