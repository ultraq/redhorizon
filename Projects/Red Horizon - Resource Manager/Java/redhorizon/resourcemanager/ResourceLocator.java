
package redhorizon.resourcemanager;

import java.io.Closeable;
import java.nio.channels.ReadableByteChannel;

/**
 * Interface to locate a named resource from an implementation-specific
 * location.
 * 
 * @author Emanuel Rabina
 */
public interface ResourceLocator extends Closeable {

	/**
	 * Search for the resource with the given name.  
	 * 
	 * @param name Name of the resource to locate.
	 * @return A byte channel into the resource with the specified name, or
	 * 		   <tt>null</tt> if the resource could not be found.
	 */
	public ReadableByteChannel locate(String name);
}
