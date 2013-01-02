
// ================================================
// Scanner's Java - Top-level animation abstraction
// ================================================

package redhorizon.media;

import redhorizon.engine.GLDrawable;
import redhorizon.engine.scenegraph.Drawable;
import redhorizon.filetypes.AnimationFile;
import redhorizon.filetypes.Paletted;
import redhorizon.filetypes.Streaming;
import redhorizon.resources.ResourceManager;
import redhorizon.utilities.DataStream;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Parent class used for all animation types, contains the most base methods
 * used for animations throughout the game.  At it's core, it's a
 * {@link Drawable} type, but extended to include play functionality.<br>
 * <br>
 * <code>Animation</code>s are also streaming-capable, to handle larger files
 * that decode their data in little bits at a time.  Streaming animations are
 * not allowed to be looped ({@link #isLooping()} will always return
 * <code>false</code> and cannot be set to <code>true</code> using
 * {@link #Animation(AnimationFile, boolean)}).<br>
 * <br>
 * For an animation, both the {@link #play()} method automatically sets the
 * animation to be visible as well as playing it.  {@link #stop()} will set the
 * animation to be invisible, as well as stop the animation.
 * 
 * @author Emanuel Rabina
 */
public class Animation extends Image {

	// Buffer at most 20 frames per streaming animation
	private static final int MAX_STREAMING_FRAMES = 20;
	private int imagesize;

	// Animation file attributes
	private final int numframes;
	private final float framerate;

	// Frame buffer -related
	private ByteBuffer[] images;
	private HashMap<Integer,ImageData> frames = new HashMap<Integer,ImageData>();
	private int currentframe;

	// Playback variables
	private final boolean looping;
	private boolean playing;
	private boolean paused;

	private final AnimationDelegator delegator;
	private AnimationListener animlistener;

	/**
	 * Constructor, takes the necessary data from the <code>AnimationFile</code>
	 * to create an animation.  Animations by default do not loop.
	 * 
	 * @param animationfile The file containing the animation.
	 */
	public Animation(AnimationFile animationfile) {

		this(animationfile, false);
	}

	/**
	 * Constructor, takes the necessary data from the <code>AnimationFile</code>
	 * to create an animation.  Specifies close-on-exit and loop
	 * characteristics.
	 * 
	 * @param animationfile The file containing the animation.
	 * @param looping		Whether to have this animation loop continuously.
	 */
	public Animation(AnimationFile animationfile, boolean looping) {

		this(animationfile, looping, null);
	}

	/**
	 * Constructor, used by other classes in this package to provide their own
	 * implementations of the <code>AnimationDelegator</code>.
	 * 
	 * @param animationfile The file containing the animation.
	 * @param looping		Whether to have this animation loop continuously.
	 * @param delegator		The <code>AnimationDelegator</code> to use.
	 */
	Animation(AnimationFile animationfile, boolean looping, AnimationDelegator delegator) {

		super(animationfile.filename(), animationfile.format(), animationfile.width(),
				animationfile.height(), null, animationfile instanceof Paletted);

		numframes = animationfile.numImages();
		framerate = animationfile.frameRate();

		boolean streaming = animationfile instanceof Streaming;
		this.looping     = streaming ? false : looping;

		this.delegator = delegator != null ? delegator : !streaming ?
				new AnimationPreload(animationfile):
				new AnimationStreaming((Streaming)animationfile);
	}

	/**
	 * Adds an implementation of the <code>AnimationListener</code> interface to
	 * be notified of special animation events.
	 * 
	 * @param listener The animation listener to notify.
	 */
	public void addListener(AnimationListener listener) {

		animlistener = listener;
	}

	/**
	 * @inheritDoc
	 */
	public void delete(GL gl) {

		delegator.delete(gl);
	}

	/**
	 * @inheritDoc
	 */
	public void init(GL gl) {

		delegator.init(gl);
	}

	/**
	 * Returns whether or not the animation is a looping one.
	 * 
	 * @return <code>true</code> if the animation loops, <code>false</code>
	 * 		   otherwise.
	 */
	public boolean isLooping() {

		return looping;
	}

	/**
	 * Returns whether or not the animation has been paused.  This means that it
	 * is in the playing state, but not moving forward.
	 * 
	 * @return <code>true</code> if the animation is paused, <code>false</code>
	 * 		   otherwise.
	 */
	public boolean isPaused() {

		return playing && paused;
	}

	/**
	 * Returns whether or not the animation is playing.
	 * 
	 * @return <code>true</code> if the animation is being played,
	 * 		   <code>false</code> otherwise.
	 */
	public boolean isPlaying() {

		return playing && !paused;
	}

	/**
	 * Pauses the animation.  Halts movement of the current frame pointer.
	 */
	public void pause() {

		paused = true;
	}

	/**
	 * Pauses the play loop.
	 */
	private synchronized void pause0() {

		while (paused) {
			wait();
		}
	}

	/**
	 * Plays the animation and makes it visible.
	 */
	public void play() {

		playAnimation();
	}

	/**
	 * Plays the animation.
	 */
	private void playAnimation() {

		currentframe = 0;
		drawing = true;
		playing = true;

		// Create gaps between moving the frame pointer
		new Thread(new Runnable() {
			public void run() {

				Thread.currentThread().setName("Animation - Play thread - " + name);

				// Calculate the gap between frames
				float nextrate = 1000f / framerate;
				long diff = System.currentTimeMillis();
				long extra = 0;
				int next;

				// Push the frame pointer along
				while (playing) {

					// When paused, append the pause time to the calculation
					if (paused) {
						long prepause = System.currentTimeMillis();
						pause0();
						extra += System.currentTimeMillis() - prepause;
					}

					// Check regularly for when the frame should be pushed ahead
					do {
						Thread.sleep(5);
						next = (int)((System.currentTimeMillis() - extra - diff) / nextrate);
					}
					while (next <= currentframe);

					// Move to the next frame or finish the animation
					if (next >= numframes) {
						break;
					}
					currentframe = looping ? next % numframes : next;
				}

				// Animation played through to the end
				if (playing) {
					stop();
				}
			}
		}).start();
	}

	/**
	 * Preload the first frame from a streaming animation.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
	void preloadFrame(GL gl) {

		waitBufferNotEmpty();
		ImageData imagedata = new ImageData(gl, removeImage(0), format, texturewidth, textureheight);
		frames.put(0, imagedata);
		widthratio  = imagedata.getWidthRatio();
		heightratio = imagedata.getHeightRatio();
	}

	/**
	 * Removes and retrieves the selected frame on the buffer.
	 * 
	 * @param thisframe Number of the frame to remove.
	 * @return The selected frame, or <code>null</code> if there was no frame at
	 * 		   the desired position.
	 */
	synchronized ByteBuffer removeImage(int thisframe) {

		ByteBuffer bufferedimage = images[thisframe];
		images[thisframe] = null;
		imagesize--;
		notifyAll();

		return bufferedimage;
	}

	/**
	 * Draws the frame currently selected by the frame pointer.
	 * 
	 * TODO: implement some sort of drawing optimization here
	 * 
	 * @param framenum The selected frame to draw.
	 * @param gl	   Current OpenGL pipeline.
	 */
	private void renderFrame(int framenum, GL gl) {

		// Select and draw the texture
		gl.glBindTexture(GL_TEXTURE_2D, frames.get(framenum).getTextureID());
		gl.glColor4f(1, 1, 1, opacity);
		gl.glBegin(GL_QUADS);
		{
			gl.glTexCoord2f(0, 0);						gl.glVertex2f(coords.getLeft(), coords.getTop());
			gl.glTexCoord2f(0, heightratio);			gl.glVertex2f(coords.getLeft(), coords.getBottom());
			gl.glTexCoord2f(widthratio, heightratio);	gl.glVertex2f(coords.getRight(), coords.getBottom());
			gl.glTexCoord2f(widthratio, 0);				gl.glVertex2f(coords.getRight(), coords.getTop());
		}
		gl.glEnd();
	}

	/**
	 * @inheritDoc
	 */
	public void render(GL gl) {

		delegator.render(gl);
	}

	/**
	 * Brings the animation out of it's paused state.
	 */
	public synchronized void resume() {

		paused = false;
		notifyAll();
	}

	/**
	 * Stops the animation and makes it invisible.
	 */
	public void stop() {

		drawing = false;
		stopAnimation();
	}

	/**
	 * Stops the animation.
	 */
	private void stopAnimation() {

		playing = false;

		// Halt streaming animation threads
		if (delegator instanceof AnimationStreaming) {
			((AnimationStreaming)delegator).datastream.stopStream();
		}

		// Notify any listeners
		if (animlistener != null) {
			animlistener.finished();
		}
	}

	/**
	 * Stores raw image bytes to the list of currently queued frames.  Waits
	 * until there is space on the buffer before storing the frame and
	 * returning.
	 * 
	 * @param thisframe		The position to store the frame.
	 * @param bufferedimage The frame to store.
	 */
	synchronized void storeImage(int thisframe, ByteBuffer bufferedimage) {

		while (imagesize >= MAX_STREAMING_FRAMES) {
			wait();
		}

		images[thisframe] = bufferedimage;
		imagesize++;
		notifyAll();
	}

	/**
	 * Returns once the buffer has at least 1 frame of data.
	 */
	private synchronized void waitBufferNotEmpty() {

		while (imagesize == 0) {
			wait();
		}
	}

	/**
	 * Interface defining tasks that the animation relies on to perform most of
	 * the methods of the animation.
	 */
	interface AnimationDelegator extends GLDrawable {
	}

	/**
	 * Worker class for pre-loaded animation files.  Assumes the workload of the
	 * methods where there can be differences between the pre-loaded and
	 * streaming animations.
	 */
	private class AnimationPreload implements AnimationDelegator {

		/**
		 * Constructor, initializes the whole image array with images from the
		 * animation file.
		 * 
		 * @param animationfile File containing the animation.
		 */
		private AnimationPreload(AnimationFile animationfile) {

			images = animationfile instanceof Paletted ?
					((Paletted)animationfile).applyPalette(ResourceManager.getPalette()):
					animationfile.getImages();
		}

		/**
		 * Because pre-loaded animations can potentially be shared, this method
		 * doesn't delete the associated textures, just it's link to them.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void delete(GL gl) {
		}

		/**
		 * Initializes all of the frames of the animation.  Preloaded animations
		 * are potentially shared, so each frame will be stored to the
		 * {@link ResourceManager}.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void init(GL gl) {

			// Create new textures for each frame
			ImageData imagedata = null;
			for (int i = 0; i < numframes; i++) {
				imagedata = new ImageData(gl, images[i], format, texturewidth, textureheight);
				frames.put(i, imagedata);
				ResourceManager.storeImageData(name + "_" + i, imagedata);
			}
			widthratio  = imagedata.getWidthRatio();
			heightratio = imagedata.getHeightRatio();

			// Drop the frame bytes
			images = null;
		}

		/**
		 * Draws the currently selected frame.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void render(GL gl) {

			// Frame to draw, avoids thread memory
			int thisframe = currentframe;

			// Draw the current frame
			if (thisframe < numframes) {
				renderFrame(thisframe, gl);
			}
			// If over the limit, hide the animation next
			else {
				erase();
			}
		}
	}

	/**
	 * Worker class for streaming animation files.  Assumes the workload of the
	 * methods where there can be differences between the pre-loaded and
	 * streaming animations.
	 */
	class AnimationStreaming implements AnimationDelegator {

		// Streaming-related
		private DataStream datastream;
		private int prevframe;

		/**
		 * Constructor, extracts the streaming interface from the animation
		 * file.
		 * 
		 * @param animationfile The file containing the animation.
		 */
		AnimationStreaming(Streaming animationfile) {

			datastream = animationfile.getDataStream();
			images = new ByteBuffer[numframes];
		}

		/**
		 * Deletes any remaining frames from the framebuffer.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void delete(GL gl) {

			// Remove remaining frame data
			for (int i = 0; i < numframes; i++) {
				removeImage(i);
			}

			// Delete all used textures
			for (ImageData frame: frames.values()) {
				if (frame != null) {
					frame.delete(gl);
				}
			}
		}

		/**
		 * Starts-off the animation decoding stream from the file, pre-render
		 * one frame.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void init(GL gl) {

			// Begin animation decoding thread
			datastream.startStream();

			// Begin bytes -> image conversion thread
			new Thread(new Runnable() {
				public void run() {

					Thread.currentThread().setName("Animation - Frame conversion thread - " + name);
					ByteBuffer[] newdata = new ByteBuffer[1];

					// Get the next frame data
					for (int i = 0; datastream.source().read(newdata); i++) {

						// Proceed only once the buffer has space
						storeImage(i, newdata[0]);
					}
				}
			}).start();

			// Preload 1 frame
			preloadFrame(gl);
		}

		/**
		 * Initializes and draws the current frame, deleting previous frames
		 * while the graphics context is current.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void render(GL gl) {

			// Current state of frame pointer to avoid thread memory issues
			int thisframe = currentframe;

			// Ready current frame if required and available
			if (!frames.containsKey(thisframe)) {
				if (images[thisframe] != null) {
					frames.put(thisframe, new ImageData(gl, removeImage(thisframe),
							format, texturewidth, textureheight));
				}
				else {
					return;
				}
			}

			// Draw the current frame
			if (thisframe < numframes) {
				renderFrame(thisframe, gl);
			}
			// If over the limit, hide the animation next
			else {
				erase();
			}

			// Delete the previous frame/s?
			if (prevframe != thisframe) {
				frames.remove(prevframe).delete(gl);
				while (prevframe < thisframe) {
					removeImage(prevframe++);
				}
			}
		}
	}
}
