
// ===============================================
// Scanner's Java - Single-player campaign manager
// ===============================================

package redhorizon.game;

import redhorizon.engine.Player;
import redhorizon.engine.scenegraph.SceneManager;
import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.filemanager.FileManager;
import redhorizon.game.map.Map;
import redhorizon.game.mission.Campaign;
import redhorizon.game.mission.CampaignDescriptor;
import redhorizon.game.mission.Mission;
import redhorizon.game.mission.MissionCallback;
import redhorizon.game.mission.MissionCallbackItems;
import redhorizon.media.Video;
import redhorizon.media.VideoListener;
import redhorizon.media.VideoScaled;
import redhorizon.misc.geometry.Point3D;
import redhorizon.settings.GameData;
import static redhorizon.game.GameObjectDepths.DEPTH_MEDIA_VIDEO;

import java.util.concurrent.Semaphore;

/**
 * Starts a new campaign for the single-player game type.  The
 * <code>SinglePlayerCampaign</code> class powers the mission-by-mission
 * structure of a campaign-based game, taking care of the interim phases (such
 * as displaying a 'pick a mission' screen), and the order of operations on the
 * mission, before playing a mission itself.  And when that's done, it repeats
 * the process all over again.
 * 
 * @author Emanuel Rabina
 */
public class SinglePlayerCampaign extends GameFlow {

	private final Player player = Player.currentPlayer();
	private final Campaign campaign;
	private Mission mission;
	private Map map;

	private boolean hasmoremissions = true;

	// Synchronization locks
	private final Semaphore missionloadedvidintro = new Semaphore(1);
	private final Semaphore missionloadedvidbriefing = new Semaphore(1);

	private String introvidname;
	private String briefingvidname;

	/**
	 * Constructor, creates a new game class for the single player campaign play
	 * type.
	 */
	public SinglePlayerCampaign() {

		// Load campaign, mission tree
		// NOTE: Need to develop the mission tree
		CampaignDescriptor campaigndesc = GameData.getLastCampaign();
		campaign = Campaign.createNewCampaign(campaigndesc);

		missionloadedvidintro.acquire();
		missionloadedvidbriefing.acquire();
	}

	/**
	 * @inheritDoc
	 */
	public boolean hasMore() {

		return hasmoremissions;
	}

	/**
	 * @inheritDoc
	 */
	protected void loadNextAsync() {

		// Load the mission, map and map objects
		mission = Mission.createMission(GameData.getNextMission(), new MissionLoadingCallback());
		map = Map.createMap(GameData.getNextMap());

		// TODO: Allow the loop to continue
		hasmoremissions = false;
	}

	/**
	 * Plays any videos/animations which are part of a pre-mission start.
	 */
	private void playAction() {

		String actionvidname = mission.getActionVideoName();
		if (actionvidname != null) {
			playVideo(new VideoScaled(FileManager.getVideoFile(actionvidname)));
		}
	}

	/**
	 * @inheritDoc
	 */
	protected void playIntro() {

		player.lockMovement();

		// Find and play the intro video, followed by the briefing video
		// TODO: Display the text briefing if no video available

		missionloadedvidintro.acquire();
		if (introvidname != null) {
			playVideo(new VideoScaled(FileManager.getVideoFile(introvidname)));
		}

		missionloadedvidbriefing.acquire();
		if (briefingvidname != null) {
			playVideo(new VideoScaled(FileManager.getVideoFile(briefingvidname)));
		}
	}

	/**
	 * Plays a video, returning once the selected video has completed play.
	 * 
	 * @param video The video to play.
	 */
	private void playVideo(Video video) {

		// Synchronization block
		final Semaphore nowplayingvid = new Semaphore(1);
		nowplayingvid.acquire();

		// Load/Play video
		SceneNode rootnode = SceneManager.currentSceneManager().getRootSceneNode();
		SceneNode vidnode = rootnode.attachChildObject(video);
		vidnode.setPosition(new Point3D(0, 0, DEPTH_MEDIA_VIDEO));

		video.addListener(new VideoListener() {
			public void finished() {
				nowplayingvid.release();
			}
		});
		video.play();

		// Stay here until video done
		nowplayingvid.acquire();
		Thread.sleep(1000);

		// Unload video
		rootnode.detachChildObject(vidnode);
	}

	/**
	 * @inheritDoc
	 */
	public void start() {

		// Clear loading elements
//		LoadingScreen.deleteLoadingScreen();
//		LoadingTheme.deleteLoadingTheme();
//		Thread.sleep(500);

		// Play 'action' video
		playAction();

		// Start jukebox track
		MusicPlayer.currentMusicPlayer().startMusic(mission.getTheme());

		// Center the player on the starting point, display the map
		player.unlockMovement();
		player.focus(map.getCameraInitCoords());
		map.show();

		// Begin mission
	}

	/**
	 * Mission-loading callback.
	 */
	private class MissionLoadingCallback implements MissionCallback {

		/**
		 * @inheritDoc
		 */
		public void loaded(MissionCallbackItems loaded, String details) {

			switch (loaded) {
			case MISSION_VIDEO_INTRO:
				introvidname = details;
				missionloadedvidintro.release();
				break;
			case MISSION_VIDEO_BRIEFING:
				briefingvidname = details;
				missionloadedvidbriefing.release();
				break;
			}
		}
	}
}
