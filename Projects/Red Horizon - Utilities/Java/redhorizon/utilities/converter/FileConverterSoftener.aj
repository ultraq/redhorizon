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

import java.lang.reflect.InvocationTargetException;

/**
 * Softens reflection exceptions in the file converters.
 * 
 * @author Emanuel Rabina
 */
public aspect FileConverterSoftener {

	/**
	 * Pointcut over all the methods in the file converters that make calls to
	 * reflection methods.
	 */
	pointcut reflection():
		call(* *.*(..) throws IllegalAccessException, InstantiationException, InvocationTargetException) &&
		within(FileConverter+);

	declare soft: IllegalAccessException:    reflection();
	declare soft: InstantiationException:    reflection();
	declare soft: InvocationTargetException: reflection();
}
