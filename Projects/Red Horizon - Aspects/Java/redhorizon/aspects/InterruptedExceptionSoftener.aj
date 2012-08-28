
package redhorizon.aspects;

/**
 * Aspect to rethrow checked thread interrupt exceptions throughout the Red
 * Horizon projects as unchecked ones.
 * 
 * @author Emanuel Rabina
 */
public aspect InterruptedExceptionSoftener {

	declare soft: InterruptedException:
		call(* *.*(..) throws InterruptedException);
}
