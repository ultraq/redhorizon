/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.engine.audio.AudioObject
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.geometry.Vector3f
import nz.net.ultraq.redhorizon.scenegraph.Playable

import static org.lwjgl.openal.AL10.*

/**
 * Basic sound in a 3D space.  Sounds are constructed from an instance of
 * {@link SoundData}, and then the data and any handles to that data are fed
 * into this class.  This class then maintains it's own set of attributes which
 * can transform the original sound when it's played.
 * 
 * @author Emanuel Rabina
 */
class SoundEffect extends Media implements AudioObject, Playable {

	// PlayableItem defaults
	Vector3f direction = new Vector3f(0,0,0)
	Vector3f velocity  = new Vector3f(0,0,0)
	boolean playing

	// Sound file attributes
	final int bitrate
	final int channels
	final int frequency

	// Sound parts
	int sourceId
	int bufferId

	/**
	 * Constructor, loads the sound from the given <code>SoundFile</code>.
	 * 
	 * @param soundfile File which should be used to construct this sound.
	 */
	SoundEffect(SoundFile soundfile) {

		super(soundfile.filename)
		bitrate   = soundfile.bitrate.size
		channels  = soundfile.channels.size
		frequency = soundfile.frequency
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void delete(AudioRenderer renderer) {

		// (Stop and) Delete the source if this sound has a handle on it
		if (alIsSource(sourceId)) {

			// Complete stop if still playing
			if (playDelay) {
				alSourceStop(sourceId)
				stop0()
			}
			alDeleteSources([sourceId])
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void init(AudioRenderer renderer) {

		// Load the appropriate sound data from the store, or create a new one
		def soundData = new SoundData(sound, bitrate, channels, frequency)
//		SoundData sounddata = ResourceManager.getSoundData(name)
//		if (sounddata == null) {
//			sounddata = new SoundData(al, sound, bitrate, channels, frequency)
//			ResourceManager.storeSoundData(name, sounddata)
//		}
		bufferId = soundData.bufferId
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void play() {

		playing = true
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void render(AudioRenderer renderer) {

		// Initial play of the sound effect
		if (playing) {

			// Generate a source if it doesn't already have one
			if (!al.alIsSource(sourceId)) {
				int[] sourceids = new int[1]
				alGenSources(1, sourceids, 0)
				sourceId = sourceids[0]
			}

			// Attach buffer to source
			alSourcei(sourceId, AL_BUFFER, bufferId)

			// Setup source attributes
			alSourceiv(sourceId, AL_POSITION, getAbsolutePosition().toArray(), 0)
			alSourcefv(sourceId, AL_DIRECTION, direction.toArray(), 0)
			alSourcefv(sourceId, AL_VELOCITY, velocity.toArray(), 0)

			// Play
			alSourcePlay(sourceId)
		}

		// Stop playing the sound effect, release the source
		else {
			alSourceStop(sourceId)
			alDeleteSources([sourceId])
			stop0()
		}

		// Check if the sound effect has stopped by itself
		int[] state = new int[1]
		alGetSourcei(sourceId, AL_SOURCE_STATE, state, 0)
		if (state[0] == AL_STOPPED) {
			stop()
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void stop() {

		playing = false
	}
}
