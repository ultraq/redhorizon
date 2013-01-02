/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import redhorizon.engine.GameEngine;
import redhorizon.ui.SplashScreen;
import redhorizon.ui.SplashScreenTask;

/**
 * Starts, runs, then shuts down the game engine.
 * 
 * @author Emanuel Rabina
 */
public class EngineTest {

	public static void main(String[] args) {

		// Open up a splash screen
		SplashScreen splashscreen = new SplashScreen(EngineTest.class.getClassLoader()
				.getResourceAsStream("Launcher_SplashScreen.png"),
				"Red Horizon Engine Test", "0.30");
		splashscreen.addTask(new SplashScreenTask() {
			@Override
			public String taskText() {
				return "Starting Red Horizon game engine";
			}
			@Override
			public void doTask() {
				new GameEngine().start();
			}
		});
		splashscreen.open();

	}
}
