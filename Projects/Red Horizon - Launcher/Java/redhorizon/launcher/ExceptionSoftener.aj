
package redhorizon.launcher;

/**
 * Softens thread interruption exceptions.
 * 
 * @author Emanuel Rabina
 */
public aspect ExceptionSoftener {

	/**
	 * Declaration to soften thread interrupted exceptions.
	 */
	declare soft: InterruptedException:
		call(public * *(..) throws InterruptedException);
}
