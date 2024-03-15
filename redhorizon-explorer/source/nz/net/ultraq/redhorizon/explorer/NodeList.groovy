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

package nz.net.ultraq.redhorizon.explorer

import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.ImGuiElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.FirstUseEver
import static imgui.flag.ImGuiStyleVar.WindowPadding
import static imgui.flag.ImGuiTreeNodeFlags.*

/**
 * An ImGUI panel showing what nodes are currently present in the scene.
 *
 * @author Emanuel Rabina
 */
class NodeList implements ImGuiElement {

	boolean enabled = true
	Scene scene

	@Override
	void render(int dockspaceId, Framebuffer sceneFramebufferResult) {

		ImGui.setNextWindowSize(300, 500, FirstUseEver)
		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.begin('Scene', new ImBoolean(true))
		ImGui.popStyleVar()

		// File list
		if (ImGui.beginListBox('##NodeList', -Float.MIN_VALUE, -Float.MIN_VALUE)) {
			if (scene) {
				scene.nodes.each { node ->
					renderNodeAndChildren(node)
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

		var flags = DefaultOpen | SpanFullWidth
		if (!node.children) {
			flags |= Leaf
		}
		if (ImGui.treeNodeEx(node.name ?: '(no name)', flags)) {
			node.children.each { child ->
				renderNodeAndChildren(child)
			}
			if (node.script) {
				if (ImGui.treeNodeEx(node.script.class.simpleName, SpanFullWidth | Leaf)) {
					ImGui.treePop()
				}
			}
			ImGui.treePop()
		}
	}
}
