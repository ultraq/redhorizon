/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.audio

import nz.net.ultraq.redhorizon.audio.Sound

import groovy.transform.TupleConstructor

/**
 * A component for adding a sound effect to an entity.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class SoundEffectComponent implements AudioComponent<SoundEffectComponent> {

	@Delegate(interfaces = false, includes = ['isPaused', 'isPlaying', 'isStopped', 'pause', 'play', 'stop'])
	final Sound sound

	@Override
	void render() {

		sound.render(entity.globalPosition)
	}
}
