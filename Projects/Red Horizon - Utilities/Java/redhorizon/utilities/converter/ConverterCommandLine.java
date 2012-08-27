
package redhorizon.utilities.converter;

import redhorizon.filetypes.WritableFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.NoSuchFileException;

/**
 * File conversion utility, based upon the Red Horizon filetypes package, to
 * transform 1 or many files into another.
 * <p>
 * The converter can only save to files which have implemented the
 * {@link WritableFile} interface.  On top of that, only some file conversions
 * are allowed; any attempt to save or perform an unsupported conversions will
 * throw an exception and the user will be notified.
 * <p>
 * Conversions currently supported by the {@link redhorizon.filetypes} package:
 * <ul>
 *   <li>C&amp;C SHP --&gt; Dune 2 SHP</li>
 *   <li>Dune 2 SHP &lt;--&gt; paletted PNG</li>
 *   <li>Dune 2 WSA &lt;--&gt; paletted PNG</li>
 *   <li>paletted PCX --&gt; CPS</li>
 *   <li>paletted PNG --&gt; C&amp;C SHP</li>
 *   <li>paletted PNG &lt;-&gt; C&amp;C WSA</li>
 *   <li>multiple paletted PNGs --&gt; C&amp;C WSA</li>
 *   <li>multiple paletted PNGs --&gt; Dune 2 SHP</li>
 * </ul>
 * Note: by 'C&amp;C' I mean Tiberium Dawn and Red Alert format.
 * 
 * @author Emanuel Rabina
 */
public class ConverterCommandLine {

	private static final Logger logger = LoggerFactory.getLogger(ConverterCommandLine.class);

	private static final String MULTIPLE_INPUTS = "(MULTI)";

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private ConverterCommandLine() {
	}

	/**
	 * Entry point for the Red Horizon file converter utility. Parameters are
	 * expected as follows:
	 * <ul>
	 *   <li>short name of the FROM type</li>
	 *   <li>short name of the TO type</li>
	 *   <li>name of the file to get the data FROM</li>
	 *   <li>name of the file to save the data TO</li>
	 * </ul>
	 * eg: <tt>java Converter pcx.PcxFile shp.ShpFileDune2 pcxfile.pcx shpfile.shp</tt>
	 * <p>
	 * Anything afterwards is an optional parameter for the TO file and works on
	 * a per-conversion basis.
	 * 
	 * @param args Command-line parameters.
	 */
	public static void main(String[] args) {

		// Check for minimum number of parameters, exit with code of 1 to let
		// batch program display 'proper use' text
		if (args.length < 4) {
			System.exit(1);
		}

		// Extract parameters
		String fromtype = args[0];
		String totype   = args[1];
		String fromfile = args[2];
		String tofile   = args[3];

		// Use multiple file input mode?
		boolean multi = false;
		if (fromtype.contains(MULTIPLE_INPUTS)) {
			fromtype = fromtype.substring(0, fromtype.indexOf(MULTIPLE_INPUTS));
			multi = true;
		}

		// Build file saving parameters
		String[] params;
		if (args.length > 4) {
			params = new String[args.length - 4];
			System.arraycopy(args, 4, params, 0, params.length);
		}
		else {
			params = new String[0];
		}

		// Perform the conversion
		try {
			FileConverter converter = multi ?
					new MultiFileConverter(fromtype, fromfile, totype, tofile, params) :
					new SingleFileConverter(fromtype, fromfile, totype, tofile, params);
			converter.convert();
		}

		// File doesn't exist / conversion not supported 
		catch (NoSuchFileException | UnsupportedConversionException ex) {
			logger.error(ex.getMessage());
			System.exit(2);
		}

		// Processing error - missing/incorrect conversion parameters
		catch (IllegalArgumentException ex) {
			logger.error(ex.getMessage());
			System.exit(1);
		}

		// All other errors
		catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(1);
		}

		// Complete
		logger.info("Done.");
	}
}
