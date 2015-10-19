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

package redhorizon.resourcemanager;

import java.io.Closeable;
import java.nio.channels.ReadableByteChannel;

/**
 * Interface to locate a named resource from an implementation-specific
 * location.
 * 
 * @author Emanuel Rabina
 */
public interface ResourceLocator extends Closeable {

	/**
	 * Search for the resource with the given name.  
	 * 
	 * @param name Name of the resource to locate.
	 * @return A byte channel into the resource with the specified name, or
	 * 		   <tt>null</tt> if the resource could not be found.
	 */
	public ReadableByteChannel locate(String name);
}
