
package redhorizon.utilities.converter;

import redhorizon.filetypes.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Converter for a single input file into a single output file.
 * 
 * @author Emanuel Rabina
 */
public class SingleFileConverter extends FileConverter {

	private static final Logger logger = LoggerFactory.getLogger(SingleFileConverter.class);

	private final String fromfile;
	private final String tofile;

	/**
	 * Constructor, sets the conversion source file, output file, and
	 * parameters.
	 * 
	 * @param fromtype	 Short name of the implementation class to convert from.
	 * @param fromfile	 Name of the input file.
	 * @param totype	 Short name of the implementation class to convert to.
	 * @param tofile	 Name of the output file.
	 * @param parameters Command-line parameters for the conversion, if any.
	 */
	public SingleFileConverter(String fromtype, String fromfile, String totype, String tofile,
		String... parameters) {

		super(fromtype, totype, parameters);
		this.fromfile = fromfile;
		this.tofile   = tofile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void convert() throws UnsupportedConversionException, IOException {

		logger.info("Loading input file/s...");
		try (File inputfile = buildInputFile(fromfile)) {
			logger.info("{}\n", inputfile);

			logger.info("Saving output file...");
			try (File outputfile = buildOutputFile(tofile, inputfile)) {
				logger.info("{}\n", outputfile);
			}
		}
		catch (NoSuchMethodException ex) {
			throw new UnsupportedConversionException("Conversion not supported", ex);
		}
	}
}
