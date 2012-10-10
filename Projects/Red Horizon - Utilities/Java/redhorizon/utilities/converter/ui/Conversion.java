
package redhorizon.utilities.converter.ui;

import java.util.Arrays;

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
	 * A conversion is equal to another if they share the same inputs and
	 * outputs.
	 * 
	 * @param other
	 * @return <tt>true</tt> if the conversion inputs and outputs match.
	 */
	@Override
	public boolean equals(Object other) {

		if (other instanceof Conversion) {
			Conversion o = (Conversion)other;
			return source.equals(o.source) && Arrays.equals(inputfiles, o.inputfiles) &&
					target.equals(o.target) && outputfile.equals(o.outputfile);
		}
		return false;
	}

	/**
	 * Creates a hashcode for the conversion, based on its inputs and outputs.
	 * 
	 * @return Conversion hashcode.
	 */
	@Override
	public int hashCode() {

		return source.hashCode() * inputfiles.hashCode() * target.hashCode() * outputfile.hashCode();
	}

	/**
	 * Returns a description of the conversion.
	 * 
	 * @return Description of the conversion.
	 */
	@Override
	public String toString() {

		StringBuilder inputs = new StringBuilder();
		if (inputfiles != null) {
			for (int i = 0; i < inputfiles.length; i++) {
				inputs.append(inputfiles[i]);
				if (i < inputfiles.length - 1) {
					inputs.append(", ");
				}
			}
		}
		return source.name + " to " + target.name + "\n" +
			"Inputs: " + (inputs.length() > 0 ? inputs : "(none)") + "\n" +
			"Output: " + (outputfile != null ? outputfile : "(none)");
	}
}
