
package redhorizon.resourcemanager;

import java.lang.reflect.ParameterizedType;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This class is the main port-of-call for retrieving and preparing any kind of
 * resource: a file, media type, entity type, etc.  By default it knows how to
 * manage files and media items, while more complex resources like object
 * instances can be managed by registering additional resource locators and
 * loaders via the {@link #addResourceLocator(String, ResourceLocator)} and
 * {@link #addResourceLoader(String, ResourceLoader)} methods.
 * 
 * @author Emanuel Rabina
 */
public class ResourceManager {

	private static final Deque<ResourceLocator> locators = new ArrayDeque<>();
	private static final LinkedHashMap<String, ResourceLocator> locatorsbyname = new LinkedHashMap<>();

	private static final LinkedHashMap<String, ResourceLoader<?>> loadersbyname = new LinkedHashMap<>();
	private static final HashMap<ResourceLoader<?>, Class<?>> loadertypes = new HashMap<>();

	private static final HashMap<String, ReadableByteChannel> locatorcache = new HashMap<>();
	private static final HashMap<String, Object> loadercache  = new HashMap<>();

	static {
		// Add a default file locator to scan the main directory
/*		addResourceLocator("_defaultFileLocator", new AbstractFileLocator() {
			@Override
			protected List<File> directories() {
				return Arrays.asList(new File(""));
			}
		});
*/
		// Register a shutdown hook to close this resource manager down cleanly
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (ResourceLocator locator: locators) {
					locator.close();
				}
			}
		});
	}

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private ResourceManager() {
	}

	/**
	 * Add a resource locator to this resource manager to be used for locating
	 * the specified resource when requested.  Resource locators are added to a
	 * stack-like data structure, so that more recent resource locators are used
	 * before older ones.
	 * 
	 * @param name	  A name to associate with the locator.  Used to identify
	 * 				  a locator when one is asked to be removed.
	 * @param locator
	 * @see #removeResourceLocator(String)
	 */
	public static void addResourceLocator(String name, ResourceLocator locator) {

		locators.push(locator);
		locatorsbyname.put(name, locator);
	}

	/**
	 * Add a resource loader to this resource manager to be used for loading a
	 * resource after having located it.
	 * 
	 * @param name	 A name to associate with the loader.  Used to identify it
	 * 				 when asked to be removed.
	 * @param loader
	 * @see #removeResourceLoader(String)
	 */
	public static void addResourceLoader(String name, ResourceLoader<?> loader) {

		// Get the generic type
		Class<?> resourcetype = (Class<?>)((ParameterizedType)loader.getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];

		loadersbyname.put(name, loader);
		loadertypes.put(loader, resourcetype);
	}

	/**
	 * Search for a resource with the given name.
	 * 
	 * @param name Name of the resource to locate.
	 * @return The resource, or <tt>null</tt> if it couldn't be found.
	 */
	public static ReadableByteChannel locateResource(String name) {

		// Check cache first for resource
		if (locatorcache.containsKey(name)) {
			return locatorcache.get(name);
		}

		// Run resource locators to find resource
		for (ResourceLocator locator: locators) {
			ReadableByteChannel bytechannel = locator.locate(name);

			// Cache and return the file
			if (bytechannel != null) {
				locatorcache.put(name, bytechannel);
				return bytechannel;
			}
		}
		return null;
	}

	/**
	 * Load a resource with the given name and type.
	 * 
	 * @param name Name of the resource to load.
	 * @param type Type of resource to load.
	 * @param <R> Resource type.
	 * @return The resource, or <tt>null</tt> if it could not be loaded.
	 */
	@SuppressWarnings("unchecked")
	public static <R> R loadResource(String name, Class<R> type) {

		R resource = null;

		// Find appropriate loader for the resource
		for (ResourceLoader<?> loader: loadersbyname.values()) {
			if (loadertypes.get(loader).isAssignableFrom(type)) {
				ResourceLoader<R> loader2 = (ResourceLoader<R>)loader;
				try {
					resource = loader2.loadResource(locateResource(name));

					// Cache and return the file
					if (resource != null) {
						loadercache.put(name, resource);
						return resource;
					}
				}
				catch (ResourceLoadingException ex) {
					// Do nothing, attempt to load using subsequent loaders
				}
			}
		}
		return null;
	}

	/**
	 * Remove a resource locator from this resource manager.
	 * 
	 * @param name Name of the resource locator to remove.
	 */
	public static void removeResourceLocator(String name) {

		ResourceLocator locator = locatorsbyname.remove(name);
		locators.remove(locator);
	}

	/**
	 * Remove a resource loader from this resource manager.
	 * 
	 * @param name Name of the resource loader to remove.
	 */
	public static void removeResourceLoader(String name) {

		loadertypes.remove(loadersbyname.remove(name));
	}
}
