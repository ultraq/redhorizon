
// ==============================
// Scanner's Java - String format
// ==============================

package redhorizon.strings;

/**
 * Interface outlining the format of anything that wants to retrieve a setting
 * value from the {@link Strings} class.
 * 
 * @author Emanuel Rabina
 */
public interface StringsKey {

	/**
	 * Returns the name of the string/parameter to retrieve.
	 * 
	 * @return <tt>String</tt> of the parameter used to map the string to.
	 */
	public String getKey();

	/**
	 * Returns the name(prefix) of the resource bundle that contains the string.
	 * 
	 * @return Name of the resource bundle that contains the string.
	 */
	public String getResourceBundle();
}
