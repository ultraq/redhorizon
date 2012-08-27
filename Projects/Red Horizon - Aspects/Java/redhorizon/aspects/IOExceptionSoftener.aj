
package redhorizon.aspects;

import java.io.IOException;

/**
 * Aspect to rethrow checked IO exceptions throughout the Red Horizon projects
 * as unchecked ones.
 * 
 * @author Emanuel Rabina
 */
public aspect IOExceptionSoftener {

	declare soft: IOException:
		call(* *.*(..) throws IOException);
}
