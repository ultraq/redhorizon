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

package nz.net.ultraq.redhorizon.graphics.imgui

import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.FirstUseEver
import static imgui.flag.ImGuiStyleVar.WindowPadding
import static imgui.flag.ImGuiTreeNodeFlags.*

/**
 * An ImGui panel showing how the scene is currently structured.
 *
 * @author Emanuel Rabina
 */
class NodeList implements ImGuiComponent {

	final boolean requiresDockspace = true
	private final Scene scene
	private Node selectedNode

	/**
	 * Constructor, tie this node list to the current ImGui context and scene.
	 */
	NodeList(Scene scene) {

		this.scene = scene
	}

	@Override
	void render() {

		ImGui.setNextWindowSize(250, 400, FirstUseEver)
		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.begin('Scene', new ImBoolean(true))
		ImGui.popStyleVar()

		// File list
		if (ImGui.beginListBox('##NodeList', -Float.MIN_VALUE, -Float.MIN_VALUE)) {
			scene.traverse { it ->
				if (it instanceof Node) {
					renderNodeAndChildren(it)
				}
			}
			ImGui.endListBox()
		}

		ImGui.end()
	}

	/**
	 * Create an entry in the UI for each node and its children.
	 */
	private void renderNodeAndChildren(Node node) {

		var flags = SpanFullWidth | OpenOnArrow | DefaultOpen
		if (!node.children) {
			flags |= Leaf
		}
		if (node == selectedNode) {
			flags |= Selected
		}
		if (ImGui.treeNodeEx(node.name ?: '(no name)', flags)) {
			if (ImGui.isItemClicked() && !ImGui.isItemToggledOpen()) {
				selectedNode = node
			}
			node.children.each { child ->
				renderNodeAndChildren(child)
			}
			ImGui.treePop()
		}
	}
}
