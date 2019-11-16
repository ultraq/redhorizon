/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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
import redhorizon.filetypes.FileType;
import redhorizon.filetypes.WritableFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Common code for the single/multi-file converters.
 * 
 * @author Emanuel Rabina
 */
public abstract class FileConverter {

	private static final Logger logger = LoggerFactory.getLogger(FileConverter.class);
	private static final String REDHORIZON_FILETYPES_PACKAGE = "redhorizon.filetypes";

	final Class<?> fromclass;
	final Class<?> toclass;
	final String[] params;

	/**
	 * Constructor, set the source and target types, and any parmeters.
	 * 
	 * @param fromtype	 Short name of the implementation class to convert from.
	 * @param totype	 Short name of the implementation class to convert to.
	 * @param parameters Command-line parameters for the conversion, if any.
	 * @throws IllegalArgumentException If the from/to classes don't exist.
	 */
	FileConverter(String fromtype, String totype, String... parameters) {

		try {
			fromclass = Class.forName(REDHORIZON_FILETYPES_PACKAGE + "." + fromtype);
			toclass   = Class.forName(REDHORIZON_FILETYPES_PACKAGE + "." + totype);
			params    = parameters;
		}
		catch (ClassNotFoundException ex) {
			logger.error("Target class doesn't exist", ex);
			throw new IllegalArgumentException("Target class doesn't exist: " + ex.getMessage());
		}
	}

	/**
	 * Create the input file type from the input file.
	 * 
	 * @param inputfilename Name of the input file to read from.
	 * @return Input file instance.
	 * @throws NoSuchMethodException If a constructor to create the file
	 * 		   instance from a file type doesn't exist.
	 * @throws IOException
	 */
	File buildInputFile(String inputfilename) throws NoSuchMethodException, IOException {

		try (FileChannel inputfilechannel = FileChannel.open(Paths.get(inputfilename))) {
			Constructor<?> fromfileconst;
			try {
				fromfileconst = fromclass.getConstructor(String.class, ReadableByteChannel.class);
			}
			catch (NoSuchMethodException ex) {
				fromfileconst = fromclass.getConstructor(String.class, FileChannel.class);
			}

			File inputfile = (File)fromfileconst.newInstance(inputfilename, inputfilechannel);
			return inputfile;
		}
	}

	/**
	 * Create the output file from the input file data.
	 * 
	 * @param outputfilename Name of the output file to save to.
	 * @param inputfiles	 One or more input file instances.
	 * @return Output file instance.
	 * @throws NoSuchMethodException If a constructor to create the file
	 * 		   instance from the input file type doesn't exist.
	 * @throws IOException
	 */
	File buildOutputFile(String outputfilename, File... inputfiles)
		throws NoSuchMethodException, IOException {

		// Create new file or overwrite old one
		try (FileChannel outputfilechannel = FileChannel.open(Paths.get(outputfilename),
				CREATE, WRITE, TRUNCATE_EXISTING)) {

			// Check target class is writable
			if (!WritableFile.class.isAssignableFrom(toclass)) {
				throw new UnsupportedOperationException();
			}

			// Get the file type to pick out the right constructor
			Class<?> targettype = fromclass.getAnnotation(FileType.class).value();
			WritableFile outputfile;

			if (inputfiles.length == 1) {
				Constructor<?> tofileconst = toclass.getConstructor(
						String.class, targettype, params.getClass());
				outputfile = (WritableFile)tofileconst.newInstance(outputfilename, inputfiles[0], params);
			}
			else {
				// Create an array of the correct type to pass to the output file constructor
				Object typearray = Array.newInstance(targettype, inputfiles.length);
				for (int i = 0; i < inputfiles.length; i++) {
					Array.set(typearray, i, inputfiles[i]);
				}
				Constructor<?> tofileconst = toclass.getConstructor(
						String.class, typearray.getClass(), params.getClass());
				outputfile = (WritableFile)tofileconst.newInstance(outputfilename, typearray, params);
			}
			outputfile.write(outputfilechannel);
			return outputfile;
		}
	}

	/**
	 * Perform the conversion.
	 * 
	 * @throws UnsupportedConversionException If the requested conversion isn't
	 * 		   supported.
	 * @throws IOException
	 */
	public abstract void convert() throws UnsupportedConversionException, IOException;
}
