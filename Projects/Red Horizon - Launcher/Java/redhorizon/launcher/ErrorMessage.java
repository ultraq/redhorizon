
// =======================================
// Scanner's Java - Error message routines
// =======================================

package redhorizon.launcher;

import java.util.ServiceLoader;

/**
 * Abstract class containing basic functionality expected of any error message
 * GUI implementations.
 * <p>
 * The default implementation uses SWT, but custom implementations can be used
 * via the standard Java SPI mechanism.  In this case, the file in
 * <tt>META-INF/services</tt> should have the same name as the fully qualified
 * name of this class (<tt>nz.net.ultraq.ui.ErrorMessage</tt>).  The first entry
 * found using this method will be used.
 * <p>
 * For this reason, implementations must provide a public no-arg constructor.
 * 
 * @author Emanuel Rabina
 * @version 1.0
 */
public abstract class ErrorMessage implements Window {

	private static ServiceLoader<ErrorMessage> serviceloader;

	/**
	 * Default constructor.
	 */
	protected ErrorMessage() {
	}

	/**
	 * Creates a new error message window built around the given error.
	 * 
	 * @param ex The exception to have displayed.
	 * @return An error message window.
	 */
	public static synchronized ErrorMessage createErrorMessage(Exception ex) {

		// Look for an external implementation first
		if (serviceloader == null) {
			serviceloader = ServiceLoader.load(ErrorMessage.class);
		}
		for (ErrorMessage errormessage: serviceloader) {
			errormessage.setErrorMessage(ex);
			return errormessage;
		}

		// Return the default implementation otherwise
		ErrorMessage errormessage = new nz.net.ultraq.gui.swt.SWTErrorMessage();
		errormessage.setErrorMessage(ex);
		return errormessage;
	}

	/**
	 * Creates the error message part from the given <tt>Exception</tt>.  This
	 * consists of the original exception name, as well as any message that was
	 * constructed with the exception.
	 * 
	 * @param ex The <tt>Exception</tt> that occurred.
	 * @return The cause and message of the exception.
	 */
	protected static String generateCause(Throwable ex) {

		// Recurse to the original exception
		Throwable cause = ex.getCause();
		if (cause != null) {
			return generateCause(cause);
		}

		// Construct exception message
		String exception = ex.toString();
		String exceptioncause;
		String exceptionreason;

		if (exception.contains(":")) {
			exceptioncause = exception.substring(0, exception.indexOf(':'));
			exceptioncause = exceptioncause.substring(exceptioncause.lastIndexOf('.') + 1);
			exceptionreason = exception.substring(exception.indexOf(':') + 2);
		}
		else {
			exceptioncause = exception.substring(exception.lastIndexOf('.') + 1);
			exceptionreason = "(none given)";
		}

		return "Cause:\t" + exceptioncause + "\n" +
			   "Reason:\t" + exceptionreason;
	}

	/**
	 * Creates the stack trace from the given <tt>Exception</tt>.  This method
	 * creates the entire trace until the exceptions no longer have a cause
	 * (when the cause == null).
	 * 
	 * @param ex The <tt>Exception</tt> that occurred.
	 * @return The stack trace of the exception.
	 */
	protected static String generateTrace(Throwable ex) {

		// Build stack trace
		String trace = ex.toString() + "\n";
		for (StackTraceElement stacktrace: ex.getStackTrace()) {
			trace += "\t" + stacktrace.toString() + "\n";
		}

		// Get the trace of the cause of this exception
		Throwable cause = ex.getCause();
		if (cause != null) {
			trace += generateTrace(cause);
		}

		return trace;
	}

	/**
	 * Sets the exception to be displayed after a call to {@link #open()}.
	 * 
	 * @param ex The exception to use in the error message window.
	 */
	protected abstract void setErrorMessage(Exception ex);
}
