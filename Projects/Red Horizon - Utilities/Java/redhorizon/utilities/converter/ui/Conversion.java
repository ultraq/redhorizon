
package redhorizon.utilities.converter.ui;

/**
 * Representation of a conversion operation, used in keeping a history.
 * 
 * @author Emanuel Rabina
 */
public class Conversion {

	final ConversionFile source;
	final String[] inputfiles;
	final ConversionFile target;
	final String outputfile;

	/**
	 * Constructor, sets up the conversion details.
	 * 
	 * @param source
	 * @param inputfiles
	 * @param target
	 * @param outputfile
	 */
	Conversion(ConversionFile source, String[] inputfiles, ConversionFile target, String outputfile) {

		this.source     = source;
		this.inputfiles = inputfiles;
		this.target     = target;
		this.outputfile = outputfile;
	}

	/**
	 * Returns a description of the conversion.
	 * 
	 * @return Description of the conversion.
	 */
	@Override
	public String toString() {

		StringBuilder inputs = new StringBuilder();
		for (int i = 0; i < inputfiles.length; i++) {
			inputs.append(inputfiles[i]);
			if (i < inputfiles.length - 1) {
				inputs.append(", ");
			}
		}
		return source.name + " to " + target.name + "\n" +
			"Inputs: " + inputs + "\n" +
			"Output: " + outputfile;
	}
}
