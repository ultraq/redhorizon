
package redhorizon.launcher;

import redhorizon.launcher.tasks.SplashScreenTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main splash screen window.
 * 
 * @author Emanuel Rabina
 */
public class SplashScreen extends Window {

	public static final int DEFAULT_WIDTH  = 600;
	public static final int DEFAULT_HEIGHT = 350;

	private final Label tasktext;
	private final ArrayList<SplashScreenTask> tasks = new ArrayList<>();
	private final ExecutorService taskexecutor = Executors.newSingleThreadExecutor();

	/**
	 * Constructor, create a new splash screen with the given image and text,
	 * set to a default size ({@link #DEFAULT_WIDTH} x {@link #DEFAULT_HEIGHT}).
	 * 
	 * @param imagepath
	 * @param text
	 * @param version
	 */
	public SplashScreen(String imagepath, String text, String version) {

		this(imagepath, text, version, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Constructor, create a new splash screen with the given image and text,
	 * set to a default size ({@link #DEFAULT_WIDTH} x {@link #DEFAULT_HEIGHT}).
	 * 
	 * @param imageinputstream
	 * @param text
	 * @param version
	 */
	public SplashScreen(InputStream imageinputstream, String text, String version) {

		this(imageinputstream, text, version, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Constructor, create a new splash screen with the given image, text, and
	 * dimensions.
	 * 
	 * @param imagepath
	 * @param text
	 * @param version
	 * @param width
	 * @param height
	 */
	@SuppressWarnings("resource")
	public SplashScreen(String imagepath, String text, String version, int width, int height) {

		this(new BufferedInputStream(new FileInputStream(imagepath)), text, version, width, height);
	}

	/**
	 * Constructor, create a new splash screen with the given image, text, and
	 * dimensions.
	 * 
	 * @param imageinputstream
	 * @param text
	 * @param version
	 * @param width
	 * @param height
	 */
	public SplashScreen(InputStream imageinputstream, String text, String version, int width, int height) {

		super(SWT.ON_TOP);

		try {
			// Splash screen frame
			shell.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			shell.setBackgroundMode(SWT.INHERIT_FORCE);
			shell.setLayout(SWTUtility.createLayout());

			// Splash screen image
			Image image = new Image(display, width, height);
			GC gc = new GC(image);
			gc.drawImage(new Image(display, imageinputstream), 0, 0, width, height, 0, 0, width, height);
			gc.dispose();
			Label splashimage = new Label(shell, SWT.NONE);
			splashimage.setImage(image);
			splashimage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			// Text area
			Composite textgroup = new Composite(shell, SWT.NO_BACKGROUND);
			textgroup.setLayout(SWTUtility.createLayout(2, false, 5, 5, 5, 5, 0, 0));
			textgroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			// Task text area
			tasktext = new Label(textgroup, SWT.NONE);
			tasktext.setText(text);
			GridData taskgriddata = new GridData(SWT.FILL, SWT.CENTER, true, false);
			tasktext.setLayoutData(taskgriddata);

			// Version text area
			Label versiontext = new Label(textgroup, SWT.NONE);
			versiontext.setText(version);
			versiontext.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			// Apply splash screen events on shell open/close
			shell.addShellListener(new ShellAdapter() {

				/**
				 * Execute all splash screen tasks in a seperate thread.
				 */
				@Override
				public void shellActivated(ShellEvent event) {

					taskexecutor.execute(new Runnable() {
						@Override
						public void run() {

							Thread.currentThread().setName("Splash Screen task execution thread");
							Thread.sleep(1000);
							for (SplashScreenTask task: tasks) {
								executeTask(task);
								Thread.sleep(500);
							}
							close();
						}
					});
				}

				/**
				 * Close the task executor.
				 */
				@Override
				public void shellClosed(ShellEvent event) {

					taskexecutor.shutdownNow();
				}
			});
		}
		finally {
			imageinputstream.close();
		}
	}

	/**
	 * Add an action to be performed once the splash screen has been opened.
	 * 
	 * @param task
	 */
	public void addTask(SplashScreenTask task) {

		tasks.add(task);
	}

	/**
	 * Execute a splash screen task on the display thread.
	 * 
	 * @param task
	 */
	private void executeTask(final SplashScreenTask task) {

		display.syncExec(new Runnable() {
			@Override
			public void run() {
				tasktext.setText(task.taskText());
			}
		});
		task.doTask();
	}
}
