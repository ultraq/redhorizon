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

package redhorizon.launcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Main GUI for reporting and displaying errors that have occured.  Contains a
 * small window to hold the error message, plus an explanation of what may have
 * been the cause.  Probably most useful to Java programmers, not users.
 * 
 * @author Emanuel Rabina
 */
@CenterShell
public class SWTErrorMessage extends ErrorMessage {

	private final Display display;
	private final Shell shell;
	private final Label messagelabel;
	private final Text tracetext;

	/**
	 * Constructor, creates an SWT error message window.
	 */
	public SWTErrorMessage() {

		display = Display.getDefault();

		// Window
		shell = new Shell(display, SWT.CLOSE | SWT.MIN | SWT.TITLE | SWT.ON_TOP);
		shell.setBackgroundMode(SWT.INHERIT_FORCE);
		shell.setText("Error Message");
		shell.setLayout(LayoutUtility.createLayoutShell());

		// Configuration group
		Composite config = new Composite(shell, SWT.NONE);
		config.setLayout(LayoutUtility.createLayoutGroup(2, false));
		config.setLayoutData(LayoutUtility.createLayoutData(LayoutUtility.HAlign.FILL, LayoutUtility.VAlign.NONE));

			// Error icon
			Label erroricon = new Label(config, SWT.CENTER);
			erroricon.setImage(display.getSystemImage(SWT.ICON_ERROR));
			erroricon.setLayoutData(LayoutUtility.createLayoutData(new Rectangle(0,0,48,48)));

			// Error text
			messagelabel = new Label(config, SWT.LEFT | SWT.WRAP);
			messagelabel.setLayoutData(LayoutUtility.createLayoutData(new Rectangle(0,0,400,SWT.DEFAULT)));

			// Stack trace text
			tracetext = new Text(config, SWT.LEFT | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
			tracetext.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			tracetext.setLayoutData(LayoutUtility.createLayoutData(new Rectangle(0,0,400,105),
					LayoutUtility.HAlign.FILL, LayoutUtility.VAlign.NONE, 2, 1));


		// Horizontal separator
		Label bar = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		bar.setLayoutData(LayoutUtility.createLayoutData(LayoutUtility.HAlign.FILL, LayoutUtility.VAlign.NONE));

		// Window controls group
		Composite controls = new Composite(shell, SWT.NONE);
		controls.setLayout(LayoutUtility.createLayoutGroup());
		controls.setLayoutData(LayoutUtility.createLayoutData(LayoutUtility.HAlign.FILL, LayoutUtility.VAlign.NONE));

			// (not)OK button
			Button okbutton = new Button(controls, SWT.PUSH | SWT.CENTER);
			okbutton.setText("OK");
			okbutton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					close();
				}
			});
			okbutton.setFocus();
			okbutton.setLayoutData(LayoutUtility.createLayoutData(new Rectangle(0,0,76,23),
					LayoutUtility.HAlign.RIGHT, LayoutUtility.VAlign.NONE, true, false));
	}

	/**
	 * @inheritDoc
	 */
	public void close() {

		shell.dispose();
	}

	/**
	 * @inheritDoc
	 */
	public void open() {

		// Window open
		shell.pack();
		shell.open();

		// Don't hog CPU cycles
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * @inheritDoc
	 */
	protected void setErrorMessage(Exception ex) {

		messagelabel.setText(ex.getMessage() + "\n\n" + generateCause(ex));
		tracetext.setText(generateTrace(ex));
	}
}
