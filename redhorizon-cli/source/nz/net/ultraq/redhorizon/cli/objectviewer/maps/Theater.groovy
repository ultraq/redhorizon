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

package nz.net.ultraq.redhorizon.cli.objectviewer.maps

import groovy.transform.TupleConstructor

/**
 * Contains a list of the available theaters used in Red Horizon.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor
enum Theater {

	// Available theater types
	DESERT    ('Desert',    '.des'),
	INTERIOR  ('Interior',  '.int'),
	SNOW      ('Snow',      '.sno', 5, 4),
	TEMPERATE ('Temperate', '.tem', 4, 4),
	WINTER    ('Winter',    '.win')

	final String label
	final String ext
	final int clearX
	final int clearY
}
