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

package redhorizon.engine;

import redhorizon.engine.audio.AudioSubsystem;
import redhorizon.engine.display.GameWindow;
import redhorizon.engine.graphics.GraphicsSubsystem;
import redhorizon.engine.input.InputSubsystem;
import redhorizon.scenegraph.Scene;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main Red Horizon game engine class, oversees the running of the game
 * engine subsystems: audio, video, input, object updating, etc.
 * 
 * @author Emanuel Rabina
 */
public class GameEngine implements SubsystemCallback {

	private final ExecutorService engineexecutor = Executors.newCachedThreadPool();
	private final CountDownLatch initbarrier = new CountDownLatch(3);

	/**
	 * Starts all engine subsystems and waits until initialization of each one
	 * is complete before returning.
	 * 
	 * @throws IllegalStateException If this method is called when this game
	 * 		   engine has already been started.
	 */
	public void start() {

		if (initbarrier.getCount() == 0) {
			throw new IllegalStateException("Game engine already started");
		}

		// Create all the subsystems and all their prerequisites, hook them all up
		// and then wait until they're all started
		Scene scene = new Scene();
		GameWindow window = new GameWindow();

		AudioSubsystem audio = new AudioSubsystem(scene, this);
		GraphicsSubsystem graphics = new GraphicsSubsystem(scene, window, this);
		InputSubsystem input = new InputSubsystem(window, this);

		engineexecutor.submit(audio);
		engineexecutor.submit(graphics);
		engineexecutor.submit(input);
		initbarrier.await();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void subsystemInit() {

		initbarrier.countDown();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void subsystemStop() {
	}
}
