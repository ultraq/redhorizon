/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package redhorizon.utilities.converter;

import redhorizon.filetypes.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;

/**
 * Converter for multiple input files into a single output file.
 * 
 * @author Emanuel Rabina
 */
public class MultiFileConverter extends FileConverter {

	private static final Logger logger = LoggerFactory.getLogger(MultiFileConverter.class);

	private final String fromfilepattern;
	private final String tofile;

	/**
	 * Constructor, sets the conversion source files, output file, and
	 * parameters.
	 * 
	 * @param fromtype		  Short name of the implementation class to convert
	 * 						  from.
	 * @param fromfilepattern File name pattern of the input files.
	 * @param totype		  Short name of the implementation class to convert
	 * 						  to.
	 * @param tofile		  Name of the output file.
	 * @param parameters	  Command-line parameters for the conversion, if any.
	 */
	public MultiFileConverter(String fromtype, String fromfilepattern, String totype, String tofile,
		String... parameters) {

		super(fromtype, totype, parameters);
		this.fromfilepattern = fromfilepattern;
		this.tofile          = tofile;
	}

	/**
	 * Builds the implementation/s for the FROM file.
	 * 
	 * @return Array of input file types for each input file.
	 * @throws NoSuchMethodException
	 * @throws IOException
	 */
	private File[] buildInputFiles() throws NoSuchMethodException, IOException {

		// Get the parts to the left and right of the numbers
		int pivot = fromfilepattern.indexOf('.');
		final String filename = fromfilepattern.substring(0, pivot);
		final String fileext  = fromfilepattern.substring(pivot);

		// Get the list of files following the given name pattern
		java.io.File dir = new java.io.File(".");
		String[] inputfilenames = dir.list(new FilenameFilter() {
			@Override
			@SuppressWarnings("hiding")
			public boolean accept(java.io.File dir, String name) {
				return name.matches(filename + "\\d{3}" + fileext);
			}
		});
		if (inputfilenames.length == 0) {
			throw new NoSuchFileException("No files found with the pattern " +
					filename + "XXX" + fileext);
		}
		Arrays.sort(inputfilenames);

		// Build files from the input files
		File[] inputfiles = new File[inputfilenames.length];
		for (int i = 0; i < inputfiles.length; i++) {
			inputfiles[i] = buildInputFile(inputfilenames[i]);
		}
		return inputfiles;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void convert() throws UnsupportedConversionException, IOException {

		logger.info("Loading input file/s...");
		try {
			File[] inputfiles = buildInputFiles();

			int pivot = fromfilepattern.indexOf('.');
			String filename = fromfilepattern.substring(0, pivot);
			String fileext  = fromfilepattern.substring(pivot);
			logger.info("{}XXX{}, ({} files)\n",
					new Object[]{ filename, fileext, inputfiles.length });

			logger.info("Saving output file...");
			try (File tofileobj = buildOutputFile(tofile, inputfiles)) {
				logger.info(tofileobj.toString());
			}

			for (File inputfile: inputfiles) {
				inputfile.close();
			}
		}
		catch (NoSuchMethodException ex) {
			throw new UnsupportedConversionException("Conversion not supported", ex);
		}
	}
}
