
// =====================================
// Scanner's Java - Mission music player
// =====================================

package redhorizon.game;

import redhorizon.engine.scenegraph.SceneManager;
import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.filemanager.FileManager;
import redhorizon.media.SoundTrack;
import redhorizon.media.SoundTrackListener;
import redhorizon.settings.Settings;
import redhorizon.xml.media.XMLSoundTrack;

import java.util.ArrayList;

/**
 * The class for controlling the music that plays during a mission.  Uses the
 * full list of music, but can then limit the playlist to include only those
 * tracks available by a number of constraints, including the player country's
 * tech level.
 * 
 * @author Emanuel Rabina
 */
public class MusicPlayer {

	// Singleton music player instance
	private static MusicPlayer musicplayer;

	private final ArrayList<XMLSoundTrack> soundtrack = new ArrayList<XMLSoundTrack>();
	private SoundTrack currenttrack;
	private int nowplaying;
	private boolean shuffle;
	private boolean repeat;
	private boolean playing;

	/**
	 * Constructor, builds a music player's playlist around the given track
	 * listing.
	 */
	private MusicPlayer() {

		// Get all soundtracks
		soundtrack.addAll(Rules.currentRules().getSoundtrack());

		// Shuffle / repeat?
		shuffle = Boolean.parseBoolean(Settings.getSetting(MusicSettingsKeys.MUSICSHUFFLE));
		repeat  = Boolean.parseBoolean(Settings.getSetting(MusicSettingsKeys.MUSICREPEAT));
	}

	/**
	 * Initializes the music player to use the tracks allocated for the current
	 * gametype.
	 * 
	 * @return The newly created musicplayer.
	 */
	public static MusicPlayer createMusicPlayer() {

		musicplayer = new MusicPlayer();
		return musicplayer;
	}

	/**
	 * Returns the currently running instance of the music player.
	 * 
	 * @return Current music player, or <code>null</code> if none exists.
	 */
	public static MusicPlayer currentMusicPlayer() {

		return musicplayer;
	}

	/**
	 * Picks another track to play, based on shuffle/repeat settings, and the
	 * constraints on the playlist.  As well as returning this track, it updates
	 * the {@link #nowplaying} to point to the returned track in the list.
	 * 
	 * @return The next soundtrack for play.
	 */
	private SoundTrack getNextTrack() {

		// Pick the index of the next track
		int nexttrackindex;
		if (shuffle) {
			do {
				nexttrackindex = (int)(Math.random() * soundtrack.size());
			}
			while (nexttrackindex != nowplaying);
		}
		else if (repeat) {
			nexttrackindex = (nowplaying + 1) % soundtrack.size();
		}
		else {
			nexttrackindex = nowplaying + 1;
		}

		nowplaying = nexttrackindex;

		// Return the soundtrack, or null if it's outside the range
		if (nowplaying < soundtrack.size()) {
			String nexttrackfile = soundtrack.get(nowplaying).getFilename();
			return new SoundTrack(FileManager.getSoundTrackFile(nexttrackfile));
		}
		return null;
	}

	/**
	 * Returns the music player's repeat state.
	 * 
	 * @return <code>true</code> if repeating is on, <code>false</code>
	 * 		   otherwise.
	 */
	public boolean isRepeat() {

		return repeat;
	}

	/**
	 * Returns the music player's shuffle state.
	 * 
	 * @return <code>true</code> if shuffling is on, <code>false</code>
	 * 		   otherwise.
	 */
	public boolean isShuffle() {

		return shuffle;
	}

	/**
	 * Plays the current {@link SoundTrack}, not returning until the
	 * {@link #stopTrackNotify()} method has been called.
	 */
	private synchronized void playTrackWait() {

		currenttrack.play();
		wait();
	}

	/**
	 * Starts the music player's automatic playlist selection, using the given
	 * <code>SoundTrack</code> as the starting point.  The player waits until
	 * that track is completed, before selecting the next song to play.  The
	 * initial track can be <code>null</code>, in which case this player will
	 * start playing a randomly selected song.
	 * 
	 * @param track Initial track to begin player with.
	 */
	public void startMusic(SoundTrack track) {

		// Prepare the next soundtrack, find the matching track (if listed)
		currenttrack = track != null ? track : getNextTrack();

		// Create a thread to take care of continuous playback
		playing = true;
		new Thread(new Runnable() {
			public void run() {
				Thread.currentThread().setName("MusicPlayer - Playback thread");

				// Create node to attach track to
				SceneNode rootnode = SceneManager.currentSceneManager().getRootSceneNode();
				SceneNode tracknode = rootnode.createChildSceneNode();

				// Continuous playlist loop
				while (playing) {

					// Ready a listener to continue playlist, play the track
					tracknode.attachObject(currenttrack);
					currenttrack.addListener(new SoundTrackListener() {
						public void finished() {
							stopTrackNotify();
						}
					});
					playTrackWait();

					// Remove the last track
					tracknode.detachObject();

					// Queue the next track
					currenttrack = getNextTrack();
					if (currenttrack == null) {
						break;
					}
				}

				// Remove track node
				rootnode.detachChildObject(tracknode);
			}
		}).start();
	}

	/**
	 * Used to indicate that the current track has stopped playing, and to
	 * wake-up the {@link #playTrackWait()} method.
	 */
	private synchronized void stopTrackNotify() {

		notifyAll();
	}
}
