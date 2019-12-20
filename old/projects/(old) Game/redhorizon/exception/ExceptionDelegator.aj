
// ===================================================
// Scanner's AspectJ - Red Horizon exception delegator
// ===================================================

package redhorizon.exception;

import redhorizon.Main;
import redhorizon.debug.DebugLog;
import redhorizon.utilities.FilePointer;
import redhorizon.utilities.FileUtility;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * As a part of the exception mechanism, all exceptions are only dealt by the
 * {@link Main} class, which in turn exits the program when an exception is
 * encountered.  Instead of cluttering methods with a declaration of the checked
 * throwables so that an exception can be propogated up the stack, this aspect
 * wraps checked exceptions in a <tt>SoftException</tt> (a type of unchecked
 * exception).
 * 
 * @author Emanuel Rabina
 */
public privileged aspect ExceptionDelegator {

	/**
	 * Soften exceptions that could occur during the use of reflection.
	 */
	pointcut reflectClass():
		call(public static Class Class.forName(String));

	pointcut reflectClassConstructor():
		call(public Constructor Class.getConstructor(Class...));

	pointcut reflectClassMethod():
		call(public Method Class.getMethod(String, Class...));

	pointcut reflectConstructorObject():
		call(public * Constructor.newInstance(Object...));

	pointcut reflectMethodInvocation():
		call(public * Method.invoke(Object, Object...));

	pointcut reflectField():
		call(public int Field.getInt(Object));

	declare soft: ClassNotFoundException:
		reflectClass();
	declare soft: NoSuchMethodException:
		reflectClassConstructor() || reflectClassMethod();
	declare soft: IllegalAccessException:
		reflectConstructorObject() || reflectMethodInvocation() || reflectField();
	declare soft: InstantiationException:
		reflectConstructorObject();
	declare soft: InvocationTargetException:
		reflectConstructorObject() || reflectMethodInvocation();

	/**
	 * Soften <tt>FileNotFoundException</tt> and <tt>IOException</tt> types on
	 * the methods of file-reading/writing classes. 
	 */
	pointcut logCreation():
		withincode(private static void DebugLog.openDebugStreams());

	pointcut filePointerInit():
		call(public FilePointer.new(..));

	pointcut fileUtilityIO():
		withincode(public static void FileUtility.copyFile*(..));

	pointcut fileReaderIO():
		call(public FileReader.new(..));

	declare soft: FileNotFoundException:
		logCreation() || filePointerInit() || fileUtilityIO();
	declare soft: IOException:
		filePointerInit() || fileUtilityIO() || fileReaderIO();

	/**
	 * Soften <tt>InterruptedException</tt>s on a threaded method, anywhere in
	 * the program.
	 */
	pointcut threadMethod():
		call(public void Thread.join(..)) || call(public static void Thread.sleep(..));

	pointcut objectMethod():
		call(public void Object.wait(..));

	pointcut semaphoreAcquire():
		call(public void acquire(..));

	declare soft: InterruptedException: threadMethod() || objectMethod() || semaphoreAcquire();
}
