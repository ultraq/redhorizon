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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.ImGuiElement

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.FirstUseEver

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * The window into which the non-persistent logs will be emitted.
 *
 * @author Emanuel Rabina
 */
class LogPanel implements ImGuiElement<LogPanel> {

	private static final int MAX_DEBUG_LINES = 100

	private final BlockingQueue<String> logLines = new ArrayBlockingQueue<>(MAX_DEBUG_LINES)

	private boolean scrollToBottom = true

	/**
	 * Constructor, create a new ImGui window for capturing and showing the logs.
	 */
	LogPanel(boolean enabled) {

		ImGuiLoggingAppender.instance.on(ImGuiLogEvent) { event ->
			if (!event.persistentKey) {
				while (!logLines.offer(event.message)) {
					logLines.poll()
				}
				scrollToBottom = true
			}
		}

		this.enabled = enabled
	}

	@Override
	void render(int dockspaceId, Framebuffer sceneFramebufferResult) {

		ImGui.setNextWindowSize(800, 300, FirstUseEver)
		ImGui.begin('Logs', new ImBoolean(true))

		if (logLines.size()) {
			ImGui.separator()
			ImGui.pushFont(ImGuiLayer.robotoMonoFont)
			logLines.each { line ->
				ImGui.selectable(line)
			}
			ImGui.popFont()

			if (scrollToBottom) {
				ImGui.setScrollHereY(1f)
			}
			scrollToBottom = false
		}

		ImGui.end()
	}
}
