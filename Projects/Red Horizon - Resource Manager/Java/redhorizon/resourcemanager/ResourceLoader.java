
package redhorizon.resourcemanager;

import java.nio.channels.ReadableByteChannel;

/**
 * Interface for resource loaders.
 * 
 * @param <R> Resource to load.
 * @author Emanuel Rabina
 */
public interface ResourceLoader<R> {

	/**
	 * Load the resource with the given name.
	 * 
	 * @param bytechannel Byte channel into the resource to load.
	 * @return The loaded resource.
	 * @throws ResourceLoadingException
	 */
	public R loadResource(ReadableByteChannel bytechannel) throws ResourceLoadingException;
}
