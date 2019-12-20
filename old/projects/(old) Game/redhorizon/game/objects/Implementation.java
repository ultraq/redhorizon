
// =======================================
// Scanner's Java - Game object archetypes
// =======================================

package redhorizon.game.objects;

/**
 * The <code>Implementation</code> is used to identify the game objects that
 * act as object blueprints from which their {@link Instance}s are born.
 * Instead of having several game objects which have similar attributes,
 * here we split them into implementations and instances to reduce game object
 * size.
 * 
 * @author Emanuel Rabina
 * @param <N> Instace type created by this implementation.
 */
public interface Implementation<N extends Instance> {

	/**
	 * Create an <code>Instance</code> of this object.  An instance is much like
	 * an object in context, which has the same potential attributes as the
	 * parent, but it's own variation on those attributes depending upon it's
	 * context.
	 * 
	 * @return An entity instance.
	 */
	public N createInstance();
}
