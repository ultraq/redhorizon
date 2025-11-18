/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.extensions

import java.util.concurrent.CompletableFuture

/**
 * Extensions to the {@link CompletableFuture} class.
 *
 * @author Emanuel Rabina
 */
class CompletableFutureExtensions {

	/**
	 * Overload of {@link CompletableFuture#allOf} to accept a list of futures
	 * instead of the JDK {@code ...} varargs method.  This is to avoid an "array
	 * length is not legal" error that seems to crop up on macOS when using
	 * Groovy's spread operator ({@code *}) over a collection type, which I am
	 * always tempted to do 😅
	 */
	static CompletableFuture<Void> allOf(CompletableFuture self, List<CompletableFuture<?>> futures) {

		return CompletableFuture.allOf(futures.toArray() as CompletableFuture<?>[])
	}
}
