
// ========================================
// Scanner's AspectJ - Return without error
// ========================================

package redhorizon.engine;

import java.io.IOException;
import java.util.Properties;

/**
 * Softens exceptions that the game engine is not too concerned about.
 * 
 * @author Emanuel Rabina
 */
public aspect GameEngineSoftener {

	/**
	 * Soften an </tt>InterruptedException</tt> on thread-based methods.
	 */
	declare soft: InterruptedException:
		call(public * *(..) throws InterruptedException);

	/**
	 * Soften IO exceptions used when loading properties files.
	 */
	declare soft: IOException:
		call(public void Properties.load(..));
}
