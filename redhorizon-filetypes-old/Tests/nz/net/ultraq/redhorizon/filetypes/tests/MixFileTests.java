/*
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes.tests;

import nz.net.ultraq.redhorizon.filetypes.mix.MixFile;
import nz.net.ultraq.redhorizon.filetypes.mix.MixRecord;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Tests related to mix files.
 * 
 * @author Emanuel Rabina
 */
public class MixFileTests {

	/**
	 * Attempts to read a Red Alert mix file: has checksum and is encrypted.
	 * 
	 * @throws IOException
	 */
	@Test
	public void readMixFile() throws IOException {

		// Open the allies mix file
		try (FileInputStream inputstream = new FileInputStream("Mods/_Red Alert/Mix/Voices_Allies.mix");
			 MixFile mixfile = new MixFile("Allies.mix", inputstream.getChannel())) {

			MixRecord record = mixfile.getEntry("affirm1.v00");
			assertNotNull(record);
		}
	}
}
