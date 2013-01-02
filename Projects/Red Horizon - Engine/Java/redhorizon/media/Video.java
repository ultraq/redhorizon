
// =====================================
// Scanner's Java - Top-level video type
// =====================================

package redhorizon.media;

import redhorizon.engine.ALPlayable;
import redhorizon.engine.GLDrawable;
import redhorizon.engine.scenegraph.Drawable;
import redhorizon.engine.scenegraph.Playable;
import redhorizon.filetypes.Streaming;
import redhorizon.filetypes.VideoFile;
import redhorizon.misc.geometry.Rectangle2D;
import redhorizon.utilities.DataStream;

import net.java.games.joal.AL;
import javax.media.opengl.GL;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Parent class used for all video types, containing the base methods used for
 * videos throughout the game.  A video consists of 2 main parts, the
 * {@link Drawable} and the {@link Playable}, which must be initialized on
 * separate threads before the video can be played.<br>
 * <br>
 * For a video, the {@link #play()} method performs the same task of playing the
 * video and making it visible/heard.  The {@link #stop()} will stop the video
 * and make it invisible/unheard.
 * 
 * @author Emanuel Rabina
 */
public class Video extends Media implements Drawable, GLDrawable, Playable, ALPlayable {

	// Tracks any videos created so that a 'current' one can be found
	private static final HashMap<String,Video> videos = new HashMap<String,Video>();

	final Animation animation;
	final SoundTrack soundtrack;

	final VideoDelegator delegator;
	VideoListener vidlistener;

	/**
	 * Constructor, takes the parts of the video from the given file.  Videos,
	 * by default, do not loop. 
	 * 
	 * @param videofile File containing the video data.
	 */
	public Video(VideoFile videofile) {

		super(videofile.filename());
		delegator = videofile instanceof Streaming ? new VideoStreaming() : null;
		animation = initAnimation(videofile);
		soundtrack = new SoundTrack(videofile, false, delegator);
		delegator.init((Streaming)videofile);

		// Wait for video end on animation stream
		animation.addListener(new AnimationListener() {
			public void finished() {
				stopVideo();
			}
		});
		// Wait for video end on soundtrack stream
		soundtrack.addListener(new SoundTrackListener() {
			public void finished() {
				stopVideo();
			}
		});

		// Register current video
		videos.put(name, this);
	}

	/**
	 * Adds an implementation of the <code>VideoListener</code> interface to be
	 * notified of special video events.
	 * 
	 * @param eventlistener The video listener to notify.
	 */
	public void addListener(VideoListener eventlistener) {

		vidlistener = eventlistener;
	}

	/**
	 * Returns the currently playing video.
	 * 
	 * @return The video that is currently playing on-screen, or
	 * 		   <code>null</code> if no video is playing.
	 */
	public static Video currentPlayingVideo() {

		for (Video video: videos.values()) {
			if (video.isPlaying()) {
				return video;
			}
		}
		return null;
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
	public void delete(GL gl) {

		delegator.delete(gl);
	}

	/**
	 * Makes the animation visible and also starts playing it.
	 */
	public void draw() {

		playVideo();
	}

	/**
	 * Makes the animation invisible and stops playing it.
	 */
	public void erase() {

		stopVideo();
	}

	/**
	 * @inheritDoc
	 */
	protected Rectangle2D getBoundingArea() {

		return animation.getBoundingArea();
	}

	/**
	 * Sets the type of {@link Animation} to use for this video.
	 * 
	 * @param videofile The file containing the video data.
	 * @return Appropriate animation for this video type.
	 */
	Animation initAnimation(VideoFile videofile) {

		return new Animation(videofile, false, delegator);
	}

	/**
	 * @inheritDoc
	 */
	public void init(AL al) {

		soundtrack.init(al);
	}

	/**
	 * @inheritDoc
	 */
	public void init(GL gl) {

		animation.init(gl);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isDrawing() {

		return animation.isDrawing();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isPlaying() {

		return animation.isPlaying() || soundtrack.isPlaying();
	}

	/**
	 * Plays the animation and makes it visible.
	 */
	public void play() {

		// Short delay on videos to allow init() to catch-up
		Thread.sleep(100);
		playVideo();
	}

	/**
	 * Makes the video visible and plays it.
	 */
	private void playVideo() {

		animation.play();
		soundtrack.play();
	}

	/**
	 * @inheritDoc
	 */
	public void render(AL al) {

		soundtrack.render(al);
	}

	/**
	 * @inheritDoc
	 */
	public void render(GL gl) {

		animation.render(gl);
	}

	/**
	 * Stops the animation and makes it invisible.
	 */
	public void stop() {

		animation.stop();
		soundtrack.stop();

		if (delegator instanceof VideoStreaming) {
			((VideoStreaming)delegator).datastream.stopStream();
		}
	}

	/**
	 * Notifies any listeners, only once both components have stopped.
	 */
	private synchronized void stopVideo() {

		// Can be called from 1 of 2 threads, so notify of stop only if needed
		if (vidlistener != null && !animation.isPlaying() && !soundtrack.isPlaying()) {
			vidlistener.finished();
		}
	}

	/**
	 * Task delegator interface for videos.  Includes both animation and
	 * soundtrack delegation tasks.
	 */
	interface VideoDelegator extends Animation.AnimationDelegator, SoundTrack.SoundTrackDelegator {

		public void init(Streaming videofile);
	}

	/**
	 * Worker class for pre-loaded video files.  Assumes the workload of the
	 * methods where there can be differences between the pre-loaded and
	 * streaming videos.
	 */
	class VideoStreaming implements VideoDelegator {

		// Streaming-related
		private DataStream datastream;
		private Animation.AnimationDelegator animstreaming;
		private SoundTrack.SoundTrackDelegator soundstreaming;

		/**
		 * Default constructor.
		 */
		VideoStreaming() {
		}

		/**
		 * Deletes both the source and the buffer IDs.
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void delete(AL al) {

			soundstreaming.delete(al);
		}

		/**
		 * Deletes any remaining frames from the framebuffer.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void delete(GL gl) {

			animstreaming.delete(gl);
		}

		/**
		 * Initializes the other delegators that the video uses.
		 * 
		 * @param videofile File containing the video.
		 */
		public void init(Streaming videofile) {

			animstreaming  = animation.new AnimationStreaming(videofile);
			soundstreaming = soundtrack.new SoundTrackStreaming(videofile);
			datastream     = videofile.getDataStream();
		}

		/**
		 * Signals that audio processing is ready, and waits until some audio
		 * data is available before proceeding.
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void init(AL al) {

			// Signal and wait
			initVideo();

			// Generate a source (priority over SFX)
			if (!al.alIsSource(soundtrack.sourceid)) {
				int[] source = new int[1];
				al.alGenSources(1, source, 0);
				soundtrack.sourceid = source[0];
			}

			// Preload 1 chunk
			soundtrack.preloadChunk(al);
		}

		/**
		 * Signals that graphics processing is ready, and waits until some
		 * graphics data is available before proceeding.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void init(GL gl) {

			// Signal and wait
			initVideo();

			// Preload 1 frame
			animation.preloadFrame(gl);
		}

		/**
		 * Starts the video decoding thread.  May be called by both audio and
		 * graphics engines, so checks if the thread has been started.  If it
		 * hasn't, then it starts it.  If it has, then it does nothing and
		 * returns.
		 */
		private synchronized void initVideo() {

			// Skip decoding start if other thread started it first
			if (datastream.isStreaming()) {
				return;
			}
			datastream.startStream();

			// Start the byte -> video conversion thread
			new Thread(new Runnable() {
				public void run() {

					Thread.currentThread().setName("Video - Data conversion thread - " + name);
					ByteBuffer[] newdata = new ByteBuffer[2];

					// Get the next sound chunk
					for (int i = 0; datastream != null && datastream.source().read(newdata); ) {

						// Store bytes
						if (newdata[0] != null) {
							animation.storeImage(i++, newdata[0]);
						}
						if (newdata[1] != null) {
							soundtrack.storeChunk(newdata[1]);
						}
					}
				}
			}).start();
		}

		/**
		 * Plays the soundtrack at first, then continues processing further
		 * buffers and queueing them onto the streaming soundtrack.
		 * 
		 * @param al Current OpenAL pipeline.
		 */
		public void render(AL al) {

			soundstreaming.render(al);
		}

		/**
		 * Initializes and draws the current frame, deleting previous frames
		 * while the graphics context is current.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void render(GL gl) {

			animstreaming.render(gl);
		}
	}
}
