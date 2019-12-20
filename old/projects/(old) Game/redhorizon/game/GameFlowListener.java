
// ====================================
// Scanner's Java - Preloading listener
// ====================================

package redhorizon.game;

/**
 * Callback interface for elements of the game which can notify that specific
 * parts have been loaded.  Used to aid the asynchronous loading process.
 * 
 * @author Emanuel Rabina
 * @param <P> The enumerated type that will be used to signal loading events.
 */
public interface GameFlowListener<P extends Enum<P>> {

	/**
	 * Called when an item of importance has been loaded.  A string identifying
	 * the item is supplied so that listeners only need to listen for the items
	 * they are interested in.
	 * 
	 * @param loaded  The identifier for the item that was just loaded.
	 * @param details Optional information about the loaded item.  eg: the
	 * 				  filename of the loaded item.  Depends on what is loaded.
	 */
	public void loaded(P loaded, String details);
}
