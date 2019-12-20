
// ========================================
// Scanner's Java - Object creation factory
// ========================================

package redhorizon.game.objects;

import redhorizon.game.faction.SubFaction;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * Generic object factory used to create the various objects found throughout
 * RA and TD.  This class is only used to assist the other factories (via
 * generics) which can pump-out more specific types.
 * 
 * @author Emanuel Rabina
 * @param <M> Implementation type.
 * @param <N> Instance type.
 * @param <X> XML binding type.
 */
public abstract class ObjectFactory<M extends Implementation<N>, N extends Instance, X> {

	// Cached implementation classes
	private final Constructor<? extends M> implconst;
	private final HashMap<String,M> impls = new HashMap<String,M>();

	/**
	 * Constructor, initializes this factory to produce the given
	 * implementations and instances.
	 * 
	 * @param implclass The class used to create new implementations.
	 * @param xmlimpl	The Java-from-XML class which matches the
	 * 					implementation.
	 */
	protected ObjectFactory(Class<? extends M> implclass, Class<? extends X> xmlimpl) {

		this.implconst = implclass.getConstructor(SubFaction.class, xmlimpl);
	}

	/**
	 * Creates a new implementation of the object type from which instances can
	 * be created.
	 * 
	 * @param subfaction Subfaction this implementation will belong to.
	 * @param id		 Object ID.
	 * @return The new implementation of the object type.
	 */
	private M createImplementation(SubFaction subfaction, String id) {

		return implconst.newInstance(subfaction, retrieveXMLData(id));
	}

	/**
	 * Returns an instance from an implementation.
	 * 
	 * @param subfaction The side this object belongs to.
	 * @param id		 Instance ID string.
	 * @return A new instance of the factory's implementation type.
	 */
	public N createInstance(SubFaction subfaction, String id) {

		String implkey = subfaction.getID() + "_" + id;

		// Create an instance from an implementation
		return impls.containsKey(implkey) ?
				impls.get(implkey).createInstance():
				createImplementation(subfaction, id).createInstance();
	}

	/**
	 * Retrieves the appropriate XML datatype to create an object
	 * implementation.
	 * 
	 * @param id The object's ID string.
	 * @return The XML datatype used to create object implementations of this
	 * 		   factory's type.
	 */
	protected abstract X retrieveXMLData(String id);
}
