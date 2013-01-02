
package redhorizon.media;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all media classes in this package.
 * 
 * @author Emanuel Rabina
 */
public abstract class Media {

	protected final String name;
	private static final AtomicInteger id = new AtomicInteger();

	/**
	 * Constructor, assigns a name to this media object.
	 * 
	 * @param name Name to assign this media object.
	 */
	protected Media(String name) {

		this.name = name + id.getAndIncrement();
	}

	/**
	 * Checks if this media item is the same as another.
	 * 
	 * @param obj
	 * @return <tt>true</tt> if both media objects are of the same type and
	 * 		   name.
	 */
	@Override
	public boolean equals(Object obj) {

		return obj != null && getClass() == obj.getClass() && name.equals(((Media)obj).name);
	}

	/**
	 * Returns the designation/name of this media type.
	 * 
	 * @return Media name.
	 */
	public String getName() {

		return name;
	}
}
