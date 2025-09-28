/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.scenegraph

/**
 * A scene is a collection of nodes, with parent/child relationships, that
 * represent the state of all or part of the game world.
 *
 * @author Emanuel Rabina
 */
class Scene {

	@Delegate(includes = ['addChild', 'leftShift', 'traverse'], interfaces = false)
	private final Node root = new RootNode()

	/**
	 * A special instance of {@link Node} that is always present in the scene.
	 */
	private class RootNode extends Node<RootNode> {

		@Override
		protected Scene getScene() {

			return Scene.this
		}

		@Override
		void traverse(SceneVisitor visitor) {

			children*.traverse(visitor)
		}
	}
}
