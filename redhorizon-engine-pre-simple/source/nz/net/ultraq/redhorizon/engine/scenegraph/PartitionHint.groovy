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

package nz.net.ultraq.redhorizon.engine.scenegraph

/**
 * A hint to the scene to classify a node with the given partition type.  This
 * can then be used to add the node to specific data structures that excel in
 * certain tasks and provide a performance boost.
 *
 * @author Emanuel Rabina
 */
enum PartitionHint {

	/**
	 * Apply no special treatment to partitioning this object.
	 */
	NONE,

	/**
	 * Dot not include this object in any partitioning algorithms.
	 */
	DO_NOT_PARTICIPATE,

	/**
	 * An object which takes up a significant amount of 2D space.
	 */
	LARGE_AREA,

	/**
	 * An object which takes up a relatively small amount of 2D space.  Nodes with
	 * this partition hint can end up in a quadtree data structure which is good
	 * for object culling.
	 */
	SMALL_AREA
}
