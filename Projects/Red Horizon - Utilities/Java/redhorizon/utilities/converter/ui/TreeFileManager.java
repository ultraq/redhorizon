/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.utilities.converter.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * An SWT Tree widget displaying the file and directory contents of your
 * computer.
 * 
 * @author Emanuel Rabina
 */
public class TreeFileManager {

	/**
	 * Constructor, creates a new file manager positioned at the root
	 * directories of a user's computer.
	 * 
	 * @param parent
	 */
	public TreeFileManager(Composite parent) {

		// Create a lazy-loaded tree
		final Tree filetree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		filetree.setHeaderVisible(true);
		filetree.addListener(SWT.Expand, new Listener() {
			@Override
			public void handleEvent(Event event) {

				TreeItem diritem = (TreeItem)event.item;

				// Remove existing tree items
				TreeItem[] existingitems = diritem.getItems();
				for (TreeItem existingitem: existingitems) {
					existingitem.dispose();
				}

				// Get and sort all items in the directory tree
				File dir = (File)diritem.getData();
				File[] files = dir.listFiles();
				Arrays.sort(files, new Comparator<File>() {
					@Override
					public int compare(File f1, File f2) {
						return f1.isDirectory() ?
								f2.isDirectory() ? f1.getName().compareTo(f2.getName()) : -1 :
								f2.isDirectory() ? 1 : f1.getName().compareTo(f2.getName());
					}
				});

				// Add all items to the directory tree
				for (File file: files) {
					TreeItem item = new TreeItem(diritem, SWT.NONE);
					item.setText(file.getName());
					item.setData(file);

					if (file.isDirectory() && file.list() != null) {
						new TreeItem(item, SWT.NONE);
					}
				}
			}
		});

		// Columns in the file manager
		TreeColumn namecolumn = new TreeColumn(filetree, SWT.LEFT);
		namecolumn.setResizable(true);
		namecolumn.setText("Name");
		namecolumn.setWidth(200);

		// Start by adding all root directories to the file manager
		File[] rootdirs = File.listRoots();
		for (File rootdir: rootdirs) {
			TreeItem dir = new TreeItem(filetree, SWT.NONE);
			dir.setText(rootdir.toString());
			dir.setData(rootdir);

			if (rootdir.list() != null) {
				new TreeItem(dir, SWT.NONE);
			}
		}
	}
}
