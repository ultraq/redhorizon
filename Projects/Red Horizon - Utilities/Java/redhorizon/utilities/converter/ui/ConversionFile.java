
package redhorizon.utilities.converter.ui;

/**
 * Available conversion source file types.
 * 
 * @author Emanuel Rabina
 */
public enum ConversionFile {

	PCX         ("PCX"),
	PNG         ("PNG"),
	PNG_MULTIPLE("PNG (Multiple)"),
	SHP         ("SHP"),
	SHP_D2      ("SHP (Dune 2)"),
	WSA         ("WSA"),
	WSA_D2      ("WSA (Dune 2)");

	public final String name;
	public final boolean multipleinputs;

	/**
	 * Constructor, sets the source name.
	 * 
	 * @param name
	 */
	private ConversionFile(String name) {

		this.name           = name;
		this.multipleinputs = name.contains("Multiple");
	}
}
