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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.engine.logback.ImGuiLogEvent
import nz.net.ultraq.redhorizon.engine.logback.ImGuiLoggingAppender
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext

import imgui.ImFont
import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.FirstUseEver

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * The window into which the non-persistent logs will be emitted.
 *
 * <p>When using the LogPanel, logback must also be configured with the
 * {@link ImGuiLoggingAppender} otherwise it will error.
 *
 * @author Emanuel Rabina
 */
class LogPanel implements ImGuiComponent {

	private static final int MAX_DEBUG_LINES = 400

	final boolean debugWindow = true
	private final BlockingQueue<String> logLines = new ArrayBlockingQueue<>(MAX_DEBUG_LINES)

	private boolean scrollToBottom = true
	private ImFont robotoMonoFont

	/**
	 * Constructor, create a new ImGui window for capturing and showing the logs.
	 */
	LogPanel() {

		ImGuiLoggingAppender.instance.on(ImGuiLogEvent) { event ->
			if (!event.persistentKey()) {
				while (!logLines.offer(event.message())) {
					logLines.poll()
				}
				scrollToBottom = true
			}
		}
	}

	@Override
	LogPanel configureFromWindow(ImGuiContext imGuiContext, Window window) {

		robotoMonoFont = imGuiContext.robotoMonoFont
		return this
	}

	@Override
	void render() {

		ImGui.setNextWindowSize(800, 300, FirstUseEver)
		ImGui.begin('Logs', new ImBoolean(true))

		if (logLines.size()) {
			ImGui.separator()
			ImGui.pushFont(robotoMonoFont)
			logLines.each { line ->
				ImGui.selectable(line)
			}
			ImGui.popFont()

			if (scrollToBottom) {
				ImGui.setScrollHereY(1f)
				scrollToBottom = false
			}
		}

		ImGui.end()
	}
}
