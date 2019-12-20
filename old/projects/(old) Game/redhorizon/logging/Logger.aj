
// ===================================
// Scanner's AspectJ - Debugger/logger
// ===================================

package redhorizon.logging;

import redhorizon.Main;

import java.io.PrintStream;

/**
 * Aspect which captures <tt>System.(out/err).print*</tt> statements and prints
 * them both to the console and a logfile.  The output string is also modified
 * to include indent information (makes reading of logging sections much easier)
 * and can also allow/disallow logging based on a tracing level (specified by
 * the {@link TraceLevel} annotation).
 * 
 * @author Emanuel Rabina
 */
public aspect Logger {

	// Global debug flag, deteremines whether or not to print/log outputs
	private static final boolean DEBUG = Boolean.getBoolean("redhorizon.debug");

	// Log and error files, log used when DEBUG is true, err used regardless.
	private static PrintStream outlog;
	private static PrintStream errlog;
	private static String gap = "";

	/**
	 * Creates the <tt>PrintStream</tt>s used as output files/logs for general
	 * output and error logging.  Redirects the <tt>System.err</tt> and
	 * <tt>System.out</tt> print streams to these files.
	 */
	before(): if(DEBUG) &&
		execution(public static void Main.main(String[])) {

		// Open log files
		outlog = new PrintStream("RedHorizon.log");
		System.setOut(outlog);
		errlog = new PrintStream("RedHorizon_Err.log");
		System.setErr(errlog);

		// Application log header
		out(
			" =====================\n" +
			" Red Horizon debug log\n" +
			" =====================\n" +
			"\n" +
			" Begin log:\n" +
			" ----------\n" +
			"\n");

		// Log system information
		out(
			" - OPERATING SYSTEM -" +
			"\n Name:         " + System.getProperty("os.name") +
			"\n Architecture: " + System.getProperty("os.arch") +
			"\n Version:      " + System.getProperty("os.version") +
			"\n\n" +
			" - JAVA RUNTIME ENVIRONMENT -" +
			"\n Version: " + System.getProperty("java.version") +
						" (" + System.getProperty("java.specification.version") + " spec)" +
			"\n Vendor:  " + System.getProperty("java.vendor") +
			"\n URL:     " + System.getProperty("java.vendor.url"));

		// Error log header
		err(
			" =====================\r\n" +
			" Red Horizon error log\r\n" +
			" =====================\r\n" +
			"\n"
		);
	}

	/**
	 * Flushes and closes the print streams.
	 */
	after(): if(DEBUG) &&
		execution(public static void Main.main(String[])) {

		out("-- End log");
		err("-- End log");
	}

	/**
	 * Log the beginning of a new phase (method annotated by the {@link Phase}
	 * annotation) and increase the logging indent.
	 * 
	 * @param phase The <tt>Phase</tt> annotation over the method.
	 */
	@SuppressWarnings("unused")
	before(Phase phase): if(DEBUG) &&
		execution(@Phase * *(..)) && @annotation(phase) {

		out(phase.name().toUpperCase() + " phase");
		gap += "  ";
	}

	/**
	 * Decrease the indent after exiting a method annotated by the {@link Phase}
	 * annotation.
	 */
	after(): if(DEBUG) &&
		execution(@Phase * *(..)) {

		gap = gap.substring(0, gap.length() - 2);
	}

	/**
	 * Log to both <tt>System.err</tt> and the error log file.
	 * 
	 * @param text String to append to the log on a new line.
	 */
	static void err(String text) {

		System.err.println(gap + text);
		errlog.println(gap + text);
	}

	/**
	 * Log to both <tt>System.out</tt> and the application log file.
	 * 
	 * @param text String to append to the log on a new line.
	 */
	static void out(String text) {

		System.out.println(gap + text);
		outlog.println(gap + text);
	}
}
