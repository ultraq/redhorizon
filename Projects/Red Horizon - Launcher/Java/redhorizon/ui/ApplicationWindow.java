/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.util.HashMap;

/**
 * A basic main application window.
 * 
 * @author Emanuel Rabina
 */
public abstract class ApplicationWindow extends Window {

	protected final MenuManager menumanager;

	/**
	 * Constructor, create a window with the usual minimize/maximize/close
	 * buttons and other optional components.
	 * 
	 * @param addmenubar Whether or not to include a menu bar in the application
	 * 					 window.
	 */
	protected ApplicationWindow(boolean addmenubar) {

		super(SWT.SHELL_TRIM);

		shell.setLayout(createFillLayout());

		menumanager = addmenubar ? new MenuManager() : null;
	}

	/**
	 * Centers the window on the current monitor.
	 */
	@Override
	protected void pack() {

		Rectangle displayarea = shell.getDisplay().getPrimaryMonitor().getBounds();
		Rectangle windowarea = shell.getBounds();
		shell.setBounds((displayarea.width - windowarea.width) / 2,
				(displayarea.height - windowarea.height) / 2,
				windowarea.width, windowarea.height);
	}

	/**
	 * Tracks menu items in the menu bar for easier access to menu items.
	 */
	protected class MenuManager {

		private final Menu menubar;
		private final HashMap<String,Menu> menus = new HashMap<>();

		/**
		 * Constructor, creates a new menu manager, adding a menu bar to the
		 * application window.
		 */
		private MenuManager() {

			menubar = new Menu(shell, SWT.BAR);
			shell.setMenuBar(menubar);
		}

		/**
		 * Adds a new menu to the menu bar.
		 * 
		 * @param displayname How the menu item appears in the menu bar,
		 * 					  including things like the accelerator key
		 * 					  (preceeded with a '&').
		 * @return The menu that was added.
		 */
		public Menu addMenu(String displayname) {

			MenuItem menubaritem = new MenuItem(menubar, SWT.CASCADE);
			menubaritem.setText(displayname);

			Menu menu = new Menu(shell, SWT.DROP_DOWN);
			menubaritem.setMenu(menu);

			String name = displayname.replace("&", "").toLowerCase();
			menus.put(name, menu);

			return menu;
		}
	}
}
