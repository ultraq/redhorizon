/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.scene

/**
 * A scene resource is any object added to a scene so that it can be used by
 * nodes, mostly in scripts.  Resources are added to the scene with {@link
 * Scene#addResource}, retrieved with {@link Scene#getResource}, or using Groovy
 * subscript operators as shorthand for either.
 *
 * @author Emanuel Rabina
 */
interface Resource {
}
