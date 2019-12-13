
// =================================
// Scanner's Java - Music/soundtrack
// =================================

package redhorizon.media;

import redhorizon.filetypes.SoundTrackFile;
import redhorizon.filetypes.Streaming;
import redhorizon.geometry.Point3D;
import redhorizon.resources.ResourceManager;
import redhorizon.utilities.DataStream;

import net.java.games.joal.AL;
import static net.java.games.joal.AL.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The <code>SoundTrack</code> is either a piece of music or sound accompanying
 * a video, with several more playback options and attributes than the parent
 * {@link SoundEffect}.<br>
 * <br>
 * <code>SoundTrack</code>s are also streaming-capable, to handle larger files
 * that decode their data in little bits at a time.  Streaming audio is not
 * allowed to be looped ({@link #isLooping()} will always return
 * <code>false</code> and cannot be set to <code>true</code> using
 * {@link #SoundTrack(SoundTrackFile, boolean)}).
 * 
 * @author Emanuel Rabina
 */
public class SoundTrack extends SoundEffect {

	// Allocate at most 10 buffers per soundtrack
	private static final int MAX_STREAMING_BUFFERS = 10;
	private int firstchunk;
	private int lastchunk;
	private int chunksize;

	// Queued buffers -related
	private ByteBuffer[] chunks;
	private Queue<SoundData> buffers = new LinkedList<SoundData>();

	// Soundtrack characteristics
	private final boolean looping;

	// Playback variables
	private boolean paused;

	private final SoundTrackDelegator delegator;
	private SoundTrackListener tracklistener;

	/**
	 * Constructor, loads the music/soundtrack from the given
	 * <code>SoundTrackFile</code>.  By default, music/soundtracks do not loop.
	 * 
	 * @param trackfile Name of the file containing the music that should be
	 * 					used to construct this soundtrack.
	 */
	public SoundTrack(SoundTrackFile trackfile) {

		this(trackfile, false);
	}

	/**
	 * Constructor, loads the music/soundtrack from the given
	 * <code>SoundTrackFile</code>, specifying whether it loops or not.
	 * 
	 * @param trackfile Name of the file containing the music that should be
	 * 					used to construct this soundtrack.
	 * @param looping	Whether or not this track should loop.
	 */
	public SoundTrack(SoundTrackFile trackfile, boolean looping) {

		this(trackfile, looping, null);
	}

	/**
	 * Constructor, used by other classes in this package to provide their own
	 * implementation of the <code>SoundTrackDelegator</code>.
	 * 
	 * @param trackfile	Name of the file containing the music that should be
	 * 					used to construct this soundtrack.
	 * @param looping	Whether or not this track should loop.
	 * @param delegator Should the internal delegator be used?
	 */
	SoundTrack(SoundTrackFile trackfile, boolean looping, SoundTrackDelegator delegator) {

		super(trackfile);
		boolean streaming = trackfile instanceof Streaming;

		this.looping = streaming ? false : looping;
		this.delegator = delegator != null ? delegator : !streaming ?
				new SoundTrackPreload():
				new SoundTrackStreaming((Streaming)trackfile);
	}

	/**
	 * Adds an implementation of the <code>SoundTrackListener</code> interface
	 * to be notified of special soundtrack events.
	 * 
	 * @param eventlistener The soundtrack listener to notify.
	 */
	public void addListener(SoundTrackListener eventlistener) {

		tracklistener = eventlistener;
	}

	/**
	 * @inheritDoc
	 */
	public void delete(AL al) {

		delegator.delete(al);
	}

	/**
	 * @inheritDoc
	 */
	public void init(AL al) {

		delegator.init(al);

		// Looping?
		if (looping) {
			al.alSourcei(sourceid, AL_LOOPING, AL_TRUE);
		}
	}

	/**
	 * Returns whether the soundtrack is a looping one.
	 * 
	 * @return <code>true</code> if this soundtrack loops, <code>false</code>
	 * 		   otherwise.
	 */
	public boolean isLooping() {

		return looping;
	}

	/**
	 * Returns whether the soundtrack is currently paused.
	 * 
	 * @return <code>true</code> if paused, <code>false</code> otherwise.
	 */
	public boolean isPaused() {

		return playing && paused;
	}

	/**
	 * Pauses the soundtrack.
	 */
	public void pause() {

		paused = true;
	}

	/**
	 * Preloads the first chunk from a streaming soundtrack.
	 * 
	 * @param al Current OpenAL pipeline.
	 */
	void preloadChunk(AL al) {

		// Enqueue at least 1 sound chunk
		waitBufferNotEmpty();
		SoundData newbuffer = new SoundData(al, removeChunk(), bitrate, channels, frequency);
		buffers.add(newbuffer);
		al.alSourceQueueBuffers(sourceid, 1, new int[]{ newbuffer.getBufferID() }, 0);
	}

	/**
	 * Removes and retrieves the earliest chunk to have been queued.
	 * 
	 * @return The earliest queued sound chunk, including <code>null</code> if
	 * 		   there are no more chunks.
	 */
	synchronized ByteBuffer removeChunk() {

		ByteBuffer bufferedchunk = chunks[firstchunk];
		firstchunk = (firstchunk + 1) % MAX_STREAMING_BUFFERS;
		chunksize--;
		notifyAll();

		return bufferedchunk;
	}

	/**
	 * @inheritDoc
	 */
	public void render(AL al) {

		delegator.render(al);
	}

	/**
	 * Resumes the soundtrack from a paused state.
	 */
	public void resume() {

		paused = false;
	}

	/**
	 * @inheritDoc
	 */
	void stop0() {

		super.stop0();

		// Stop streaming soundtrack threads
		if (delegator instanceof SoundTrackStreaming) {
			((SoundTrackStreaming)delegator).datastream.stopStream();
		}

		// Notify any event listeners
		if (tracklistener != null) {
			tracklistener.finished();
		}
	}

	/**
	 * Saves a sound chunk object to the list of currently queued chunks.  If
	 * there is no space on the queue, waits until there is before storing the
	 * chunk and returning.
	 * 
	 * @param bufferedchunk The sound chunk to store.
	 */
	synchronized void storeChunk(ByteBuffer bufferedchunk) {

		while (chunksize >= MAX_STREAMING_BUFFERS) {
			wait();
		}

		chunks[lastchunk] = bufferedchunk;
		lastchunk = (lastchunk + 1) % MAX_STREAMING_BUFFERS;
		chunksize++;
		notifyAll();
	}

	/**
	 * Returns once the buffer has at least 1 chunk of data.
	 */
	private synchronized void waitBufferNotEmpty() {

		while (chunksize == 0) {
			wait();
		}
	}

	/**
	 * Interface defining tasks that the soundtrack relies on to perform most of
	 * the methods in the soundtrack.
	 */
	interface SoundTrackDelegator extends ALPlayable {
	}

	/**
	 * Worker class for pre-loaded soundtrack files.  Assumes the workload of
	 * the methods where there can be differences between the pre-loaded and
	 * streaming soundtracks.
	 */
	private class SoundTrackPreload implements SoundTrackDelegator {

		/**
		 * Default constructor.
		 */
		private SoundTrackPreload() {
		}

		/**
		 * Deletes the source (if needed) and nothing else.  Pre-loaded
		 * soundtracks can potentially be shared, so this method does nothing.
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void delete(AL al) {

			SoundTrack.super.delete(al);
		}

		/**
		 * Initializes the entire soundtrack.
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void init(AL al) {

			// Create the single buffer, store it
			SoundData sounddata = new SoundData(al, sound, bitrate, channels, frequency);
			buffers.add(sounddata);
			ResourceManager.storeSoundData(name, sounddata);

			// Drop the sound bytes
			sound = null;
		}

		/**
		 * Plays the full soundtrack.
		 * TODO: Update position only as necessary
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void render(AL al) {

			if (playdelay) {

				// Stop playing the soundtrack, release the source
				if (!playing) {
					al.alSourceStop(sourceid);
					al.alDeleteSources(1, new int[]{ sourceid }, 0);
					stop0();
				}

				// Pause the soundtrack
				else if (paused) {
					al.alSourcePause(sourceid);
					stop0();
				}

				// Check if the sound effect has stopped by itself
				else {
					int[] state = new int[1];
					al.alGetSourcei(sourceid, AL_SOURCE_STATE, state, 0);
					if (state[0] == AL_STOPPED) {
						stop();
					}

					// Reposition mono sound sources
					if (channels == 1) {
						Point3D listenerpos = Listener.currentListener().getPosition();
						al.alSourceiv(sourceid, AL_POSITION, listenerpos.toArray(), 0);
					}
				}
			}
			else {

				// Play/resume the soundtrack
				if (playing && !paused) {

					// Generate a source if it doesn't already have one
					if (!al.alIsSource(sourceid)) {
						int[] sourceids = new int[1];
						al.alGenSources(1, sourceids, 0);
						sourceid = sourceids[0];

						// Attach the source to the buffered data (static source)
						al.alSourcei(sourceid, AL_BUFFER, buffers.remove().getBufferID());
					}

					al.alSourcePlay(sourceid);
					play0();
				}
			}
		}
	}

	/**
	 * Worker class for streaming soundtrack files.  Assumes the workload of the
	 * methods where there can be differences between the pre-loaded and
	 * streaming soundtracks.
	 */
	class SoundTrackStreaming implements SoundTrackDelegator {

		// Streaming-related
		private DataStream datastream;

		/**
		 * Hidden constructor, extracts the streaming interface from the sound
		 * file.
		 * 
		 * @param soundfile The file containing the soundtrack.
		 */
		SoundTrackStreaming(Streaming soundfile) {

			datastream = soundfile.getDataStream();
			chunks = new ByteBuffer[MAX_STREAMING_BUFFERS];
		}

		/**
		 * Deletes both the source and the buffer IDs.
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void delete(AL al) {

			// Complete stop if still playing
			if (playdelay) {
				al.alSourceStop(sourceid);
				stop0();
			}

			// Detach source from buffers, then release source
			al.alSourcei(sourceid, AL_BUFFER, 0);
			al.alDeleteSources(1, new int[]{ sourceid }, 0);

			// Remove remaining sound data
			while (chunksize > 0) {
				removeChunk();
			}

			// Delete buffers
			for (SoundData buffer: buffers) {
				buffer.delete(al);
			}
		}

		/**
		 * Initializes the audio decoding stream from the file.
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void init(AL al) {

			// Start the sound decoding thread
			datastream.startStream();

			// Begin bytes -> sound conversion thread
			new Thread(new Runnable() {
				public void run() {

					Thread.currentThread().setName("SoundTrack - Buffer conversion thread - " + name);
					ByteBuffer[] newdata = new ByteBuffer[1];

					// Get the next sound chunk
					while (datastream.source().read(newdata)) {

						// Store only once the buffer has space
						storeChunk(newdata[0]);
					}
				}
			}).start();

			// Generate a source (priority over SFX)
			if (!al.alIsSource(sourceid)) {
				int[] source = new int[1];
				al.alGenSources(1, source, 0);
				sourceid = source[0];
			}
			// Preload 1 chunk
			preloadChunk(al);
		}

		/**
		 * Plays the soundtrack at first, then continues processing further
		 * buffers and queueing them onto the streaming soundtrack.
		 * 
		 * TODO: Update position only as necessary
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void render(AL al) {

			if (playdelay) {

				// Stop sound processing
				if (!playing) {
					al.alSourceStop(sourceid);
					stop0();
				}

				// Pause sound processing
				else if (paused) {
					al.alSourcePause(sourceid);
					stop0();
				}

				// Continue sound processing
				else {

					// Queue additional buffer if able
					if (chunksize > 0 && buffers.size() < MAX_STREAMING_BUFFERS) {
						SoundData newbuffer = new SoundData(al, removeChunk(), bitrate, channels, frequency);
						buffers.add(newbuffer);
						al.alSourceQueueBuffers(sourceid, 1, new int[]{ newbuffer.getBufferID() }, 0);
					}

					// Unqueue and delete previous buffers
					int[] numprocessed = new int[1];
					al.alGetSourcei(sourceid, AL_BUFFERS_PROCESSED, numprocessed, 0);
					if (numprocessed[0] > 0) {
						SoundData usedbuffer = buffers.remove();
						al.alSourceUnqueueBuffers(sourceid, 1, new int[]{ usedbuffer.getBufferID() }, 0);
						usedbuffer.delete(al);
					}

					// Check if the soundtrack has stopped by itself
					int[] state = new int[1];
					al.alGetSourcei(sourceid, AL_SOURCE_STATE, state, 0);
					if (state[0] == AL_STOPPED || state[0] == AL_INITIAL) {
						stop();
					}

					// Reposition mono sound sources
					if (channels == 1) {
						Point3D listenerpos = Listener.currentListener().getPosition();
						al.alSourceiv(sourceid, AL_POSITION, listenerpos.toArray(), 0);
					}
				}
			}
			else {

				// Play/resume the soundtrack
				if (playing && !paused) {
					al.alSourcePlay(sourceid);
					play0();
				}
			}
		}
	}
}
