/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

/**
 * A collection of classes made to read from / write to classic C&C file
 * formats.
 * <p>
 * Reading is relatively straightforward, usually supplying an {@link java.io.InputStream}
 * to the constructor of the matching file class will read the file and then
 * give you an object with access to the file's underlying data.
 * <p>
 * Writing is done on a case-by-case basis, usually by trying to fulfil the file
 * conversion utilities from a much older version of this project.
 */
package nz.net.ultraq.redhorizon.classic.filetypes;
