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

package redhorizon.utilities;

/**
 * Timed event handler interface for repeat tasks.
 * 
 * @author Emanuel Rabina
 */
public interface AnimatorTask extends TimerTask {

	/**
	 * Called when an animation begins.
	 */
	@Override
	public void begin();

	/**
	 * Called when a animation is complete.
	 */
	public void end();

	/**
	 * Called at regular intervals, by the {@link Animator}, notifies the
	 * handler implementation of how far along the animation has progressed.
	 * 
	 * @param fraction The fraction of the current animation cycle that has been
	 * 				   completed.
	 */
	public void event(float fraction);
}
