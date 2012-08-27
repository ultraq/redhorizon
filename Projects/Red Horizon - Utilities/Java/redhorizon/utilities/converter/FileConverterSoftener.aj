
package redhorizon.utilities.converter;

import java.lang.reflect.InvocationTargetException;

/**
 * Softens reflection exceptions in the file converters.
 * 
 * @author Emanuel Rabina
 */
public aspect FileConverterSoftener {

	/**
	 * Pointcut over all the methods in the file converters that make calls to
	 * reflection methods.
	 */
	pointcut reflection():
		call(* *.*(..) throws IllegalAccessException, InstantiationException, InvocationTargetException) &&
		within(FileConverter+);

	declare soft: IllegalAccessException:    reflection();
	declare soft: InstantiationException:    reflection();
	declare soft: InvocationTargetException: reflection();
}
