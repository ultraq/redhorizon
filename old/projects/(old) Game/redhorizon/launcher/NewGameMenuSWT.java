
// ==================================================
// Scanner's Java - Campaign/Mission selection window
// ==================================================

package redhorizon.launcher;

import redhorizon.Main;
import redhorizon.game.mission.CampaignDescriptor;
import redhorizon.game.mission.MissionDescriptor;
import redhorizon.misc.CNCGameTypes;
import redhorizon.strings.Strings;
import redhorizon.utilities.SWTLayouts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This is where the player will be forwarded to when they press 'New Game' in
 * the main menu.  From here, the player can see the list of installed
 * campaigns, as well as the missions available to them.
 * 
 * @author Emanuel Rabina
 */
class NewGameMenuSWT extends NewGameMenu {

	private final Shell shell;
//	private final List  campaignlist;
	private final Tree  campaignlist;
	private final Label campaigndesc;
	private final List  missionlist;

	private CNCGameTypes selectedgametype;
	private CampaignDescriptor selectedcampaign;
	private MissionDescriptor selectedmission;

	/**
	 * Constructor, creates a new game window.
	 * 
	 * @param display SWT <tt>Display</tt> handle.
	 */
	NewGameMenuSWT(Display display) {

		// New game background image
		Image raven = new Image(display, "Resources/Launcher/Splash_Raven.png");
		Image title = new Image(display, "Resources/Launcher/Text_NewGameMenu.png");
		Image image = new Image(display, 450, 500);
		GC gc = new GC(image);
		gc.drawImage(raven, 25, 35, 450, 100, 0, 0, 450, 100);
		gc.drawImage(title, 337, 83);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 100, 450, 312);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 412, 450, 50);

		// Window
		shell = new Shell(display, SWT.CLOSE | SWT.MIN | SWT.TITLE);
		shell.setText(Strings.getText(LauncherStringsKeys.GAMENEW_WINDOW_TITLE) + " - " + Main.getName());
		shell.setLayout(SWTLayouts.createLayoutShell());

		// Title image
		Label titleimg = new Label(shell, SWT.NONE);
		titleimg.setBackgroundImage(image);
		titleimg.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,450,100)));

		// Horizontal separator
		Label bar1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		bar1.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

		// Configuration group
		Composite config = new Composite(shell, SWT.NONE);
		config.setLayout(SWTLayouts.createLayoutGroup(2, true));
		config.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

			// Campaign label
			Label campaignlabel = new Label(config, SWT.LEFT);
			campaignlabel.setText(Strings.getText(LauncherStringsKeys.GAMENEW_GROUP1_HEADER));

			// Mission label
			Label missionlabel = new Label(config, SWT.LEFT);
			missionlabel.setText(Strings.getText(LauncherStringsKeys.GAMENEW_GROUP2_HEADER));

			// Campaign/mod list
			campaignlist = new Tree(config, SWT.SINGLE | SWT.BORDER);
			campaignlist.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			for (CNCGameTypes gametype: CNCGameTypes.values()) {
				TreeItem gametypetree = new TreeItem(campaignlist, SWT.NONE);
				gametypetree.setText(gametype.name);
				for (CampaignDescriptor campdesc: campaigns.get(gametype)) {
					TreeItem campaigntree = new TreeItem(gametypetree, SWT.NONE);
					campaigntree.setText(campdesc.getName());
				}
				gametypetree.setExpanded(true);
			}
			campaignlist.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,113),
					SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.FILL, true, true));
			campaignlist.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					campaignSelected();
				}
			});

			// Mission list
			missionlist = new List(config, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
			missionlist.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			missionlist.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,113),
					SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.FILL, true, true));
			missionlist.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					missionSelected();
				}
			});

			// Group section - campaign details
			Group detailgroup = new Group(config, SWT.V_SCROLL);
			detailgroup.setText(Strings.getText(LauncherStringsKeys.GAMENEW_DESC_DETAILS));
			detailgroup.setLayout(SWTLayouts.createLayoutInternal());
			detailgroup.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,120),
					SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE, 2, 1));

			// Campaign/mod description
			campaigndesc = new Label(detailgroup, SWT.LEFT | SWT.WRAP);
			campaigndesc.setLayoutData(SWTLayouts.createLayoutData(
					SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.FILL, true, true));


		// Horizontal separator
		Label bar2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		bar2.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

		// Window controls group
		Composite controls = new Composite(shell, SWT.NONE);
		controls.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		controls.setBackgroundMode(SWT.INHERIT_FORCE);
		controls.setLayout(SWTLayouts.createLayoutGroup(3, false));
		controls.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

			// Blank area?
			Label blank = new Label(controls, SWT.NONE);
			blank.setLayoutData(SWTLayouts.createLayoutData(true, true));

			// Play button
			Button playbutton = new Button(controls, SWT.PUSH);
			playbutton.setText(Strings.getText(LauncherStringsKeys.BUTTON_PLAY));
			playbutton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					playSelected();
				}
			});
			playbutton.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,76,23),
					SWTLayouts.HAlign.RIGHT, SWTLayouts.VAlign.NONE, true, false));

			// Cancel button
			Button cancelbutton = new Button(controls, SWT.PUSH);
			cancelbutton.setText(Strings.getText(LauncherStringsKeys.BUTTON_CANCEL));
			cancelbutton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					cancelSelected();
				}
			});
			cancelbutton.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,76,23),
					SWTLayouts.HAlign.RIGHT, SWTLayouts.VAlign.NONE));


		// Open the window
		shell.pack();
		shell.open();

		// Don't hog CPU cycles
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		// Dispose of programmer-created resources
		gc.dispose();
		image.dispose();
		title.dispose();
		raven.dispose();
	}

	/**
	 * Method for when the user selects a campaign from the list.  The GUI will
	 * show the description of the selected campaign in a dedicated description
	 * space.
	 */
	private void campaignSelected() {

		// Discard missed clicks
		if (campaignlist.getSelectionCount() == 0) {
			selectedgametype = null;
			selectedcampaign = null;
			campaigndesc.setText("");
			missionlist.removeAll();
			return;
		}
		// Quit if the selected item wasn't a campaign name
		String selected = campaignlist.getSelection()[0].getText();
		for (CNCGameTypes gametype: CNCGameTypes.values()) {
			if (gametype.name.equals(selected)) {
				campaigndesc.setText("");
				missionlist.removeAll();
				return;
			}
		}

		// Set the campaign description
		outer: for (CNCGameTypes gametype: CNCGameTypes.values()) {
			for (CampaignDescriptor campdesc: campaigns.get(gametype)) {
				if (campdesc.getName().equals(selected)) {
					campaigndesc.setText(campdesc.getDescription());

					selectedgametype = gametype;
					selectedcampaign = campdesc;
					break outer;
				}
			}
		}

		// Refresh the mission list
		missionlist.setItems(missionStringArray());
	}

	/**
	 * Closes the New Game Menu.
	 */
	private void cancelSelected() {

		shell.dispose();
	}

	/**
	 * Picks the appropriate mission descriptor when a mission is selected from
	 * the list.
	 */
	private void missionSelected() {

		// Discard missed clicks
		if (missionlist.getSelectionCount() == 0 && selectedcampaign != null) {
			selectedmission = null;
			return;
		}

		// Set the currently selected mission
		selectedmission = selectedcampaign.getMissionDescriptors().get(missionlist.getSelectionIndex());
	}

	/**
	 * Converts the mission list of the selected campaign into a
	 * <tt>String[]</tt> form required by SWT.  The order of the original list
	 * is maintained, so that access to the list can be matched by the index
	 * value of the returned array.

	 * @return <tt>String</tt>[] of the mission names.
	 */
	private String[] missionStringArray() {

		java.util.List<MissionDescriptor> missions = selectedcampaign.getMissionDescriptors();
		String[] missionnames = new String[missions.size()];

		// Display 'new game' part if there is only 1 mission in the list
		if (missions.size() == 1) {
			missionnames[0] = missions.get(0).getName();
		}

		// Create mission names as "File - Name"
		else {
			for (int i = 0; i < missions.size(); i++) {
				MissionDescriptor mission = missions.get(i);
				missionnames[i] = mission.getFile() + " - " + mission.getName();
			}
		}
		return missionnames;
	}

	/**
	 * Sets flags to indicate that a campaign and mission have been selected to
	 * play.  Closes this and any other open menus.
	 */
	private void playSelected() {

		// Don't proceed if a campaign and mission aren't selected
		if (selectedcampaign == null || selectedmission == null) {
			return;
		}

		setGameToPlay(selectedgametype, selectedcampaign, selectedmission);
		exitcode = true;
		shell.dispose();
	}
}
