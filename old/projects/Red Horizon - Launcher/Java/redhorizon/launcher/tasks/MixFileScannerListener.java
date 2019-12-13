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

package redhorizon.launcher.tasks;

import redhorizon.filetypes.mix.MixFile;
import redhorizon.filetypes.mix.MixRecord;
import redhorizon.resourcemanager.ResourceLocator;
import redhorizon.resourcemanager.scanner.ScannerListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Scanner listener that loads information from MIX files when they are
 * encountered.
 * 
 * @author Emanuel Rabina
 */
public class MixFileScannerListener implements ScannerListener, ResourceLocator {

	private static final Logger logger = LoggerFactory.getLogger(MixFileScannerListener.class);

	private static final Pattern MIX_FILE_PATTERN = Pattern.compile(".*\\.mix");

	private final ArrayList<MixFile> mixfiles = new ArrayList<>();
	private final HashMap<String,MixFileRecord> cachedrecords = new HashMap<>();

	/**
	 * Close all of the mix files tracked by this scanner.
	 */
	@Override
	public void close() {

		for (MixFile mixfile: mixfiles) {
			mixfile.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel locate(String name) {

		// Check cache first
		if (cachedrecords.containsKey(name)) {
			MixFileRecord filerecord = cachedrecords.get(name);
			return filerecord.file.getEntryData(filerecord.record);
		}

		// Check MIX files next
		for (MixFile mixfile: mixfiles) {
			MixRecord record = mixfile.getEntry(name);
			if (record != null) {
				cachedrecords.put(name, new MixFileRecord(mixfile, record));
				return mixfile.getEntryData(record);
			}
		}

		return null;
	}

	/**
	 * Tracks encountered mix files.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void match(Path path) {

		String mixfilename = path.getFileName().toString();
		logger.info("Loading {}", mixfilename);
		mixfiles.add(new MixFile(mixfilename, FileChannel.open(path)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pattern pattern() {

		return MIX_FILE_PATTERN;
	}

	/**
	 * Pairs a mix file with a record since there isn't normally an association
	 * between the two.
	 */
	private class MixFileRecord {

		private final MixFile file;
		private final MixRecord record;

		/**
		 * Constructor, ties a file to a record.
		 * 
		 * @param file
		 * @param record
		 */
		private MixFileRecord(MixFile file, MixRecord record) {

			this.file   = file;
			this.record = record;
		}
	}
}
