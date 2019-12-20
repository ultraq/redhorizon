
// =================================================
// Scanner's AspectJ - Debug outputs to file/console
// =================================================

package redhorizon.debug;

import redhorizon.Main;
import redhorizon.engine.Capabilities;
import redhorizon.engine.MissingCapabilityException;
import redhorizon.exception.ExceptionWrapper;
import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.File;
import redhorizon.filetypes.ini.IniFile;
import redhorizon.game.SinglePlayerCampaign;
import redhorizon.game.faction.SubFaction;
import redhorizon.game.map.Map;
import redhorizon.game.map.MapRA;
import redhorizon.game.map.MapPackRA;
import redhorizon.game.map.OverlayPackRA;
import redhorizon.game.map.TerrainRA;
import redhorizon.game.objects.Implementation;
import redhorizon.game.objects.ObjectFactory;
import redhorizon.game.objects.units.UnitFactory;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL.*;

import net.java.games.joal.AL;
import net.java.games.joal.ALFactory;
import static net.java.games.joal.AL.*;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * This aspect deals with the printing/output of programming messages to either
 * the console or to a log file.  These are mainly messages intended for the
 * programmer; not in-game messages such as mission objectives, etc.<br>
 * <br>
 * Notes: Exceptions and program errors are shown regardless.
 * 
 * @author Emanuel Rabina
 */
public privileged aspect DebugLog {

	/**
	 * Logs information on the sound hardware used.
	 * 
	 * @param alextlist List of OpenAL extensions supported by this platform.
	 */
	@SuppressWarnings("unused")
	after(Object[] alextlist): if (debug) &&
		call(public static void Arrays.sort(Object[])) && args(alextlist) &&
		withincode(static void Capabilities.testAL(AL)) {

		AL al = ALFactory.getAL();

		// Get information of the sound card being used
		appendLog("- AUDIO DEVICE -");
		appendLog("Vendor: " + al.alGetString(AL_VENDOR));
		appendLog("Device name: " + al.alGetString(AL_RENDERER));
		appendLog("OpenAL version: " + al.alGetString(AL_VERSION));

		// And the list of supported OpenAL capabilities
		appendLog("Supported OpenAL extensions:");
		for (String alext: (String[])alextlist) {
			appendLog(" - " + alext);
		}
		appendLog();
	}

	/**
	 * Logs information on the graphics hardware used.
	 * 
	 * @param glextlist List of OpenGL extensions supported by this platform.
	 */
	@SuppressWarnings("unused")
	after(Object[] glextlist): if (debug) &&
		call(public static void Arrays.sort(Object[])) && args(glextlist) &&
		withincode(static void Capabilities.testGL(GL)) {

		GL gl = GLU.getCurrentGL();

		// Get information of the video card being used
		appendLog("- GRAPHICS DEVICE -");
		appendLog("Vendor: " + gl.glGetString(GL_VENDOR));
		appendLog("Device name: " + gl.glGetString(GL_RENDERER));
		appendLog("OpenGL version: " + gl.glGetString(GL_VERSION));

		// And the list of supported OpenGL capabilities
		appendLog("Supported (non-vendor) OpenGL extensions:");
		for (String glext: (String[])glextlist) {
			if (glext.startsWith("GL_ARB") || glext.startsWith("GL_EXT")) {
				appendLog(" - " + glext);
			}
		}
		appendLog();
	}

	/**
	 * Log pass/fail of user hardware tests.
	 * 
	 * @throws MissingCapabilityException
	 */
	void around() throws MissingCapabilityException: if (debug) &&
		call(static void Capabilities.testAL(AL)) || call(static void Capabilities.testGL(GL)) {

		appendLog("Testing " + (thisJoinPointStaticPart.getSignature().getName().endsWith("AL") ?
				  "sound" : "video") + " hardware...");
		appendLog();
		gapPlus();

		try {
			proceed();
			appendLog("...passed.");
		}
		catch (MissingCapabilityException mcex) {
			appendLog("...failed.");
			throw mcex;
		}
		finally {
			gapMinus();
		}
	}

	/**
	 * Log startup/shutdown of initialization/closing items within the game
	 * code.
	 */
	void around(): if (debug) &&
		within(Main) && (call(public static void *.init()) || call(public static void *.close())) {

		// Joinpoint signatures
		String longname   = thisJoinPointStaticPart.getSignature().getDeclaringTypeName();
		String classname  = longname.substring(longname.lastIndexOf('.') + 1);
		String methodname = thisJoinPointStaticPart.getSignature().getName();
		boolean init = methodname.equals("init");

		// Entry log
		appendLog(classname + ": " + (init ? "Initializing..." : "Closing and/or saving state..."));
		gapPlus();

		proceed();

		// Exit log
		appendLog("...done.");
		gapMinus();
	}

	/**
	 * Log the loading of any files/archives into the {@link FileManager}.
	 */
	before(): if (debug) &&
		execution(private static void FileManager.load*(..)) {

		Object[] params = thisJoinPoint.getArgs();
		String filename = ((File)params[params.length - 1]).filename();
		String filetype = thisJoinPointStaticPart.getSignature().getName();
		boolean archive = filetype.contains("Archive");

		appendLog("Loading " + (archive ? "archive: " : "file: ") + filename + "...");
	}

	/**
	 * Log creation of new maps/missions.
	 * 
	 * @param map	  The map object being created.
	 * @param mapfile The file the map is being constructed from.
	 */
	@SuppressWarnings("unused")
	void around(Map map, IniFile mapfile): if (debug) &&
		execution(MapRA.new(IniFile)) && target(map) && args(mapfile) {

		appendLog("Creating new Red Alert map from file " + mapfile.filename() + "...");
		gapPlus();

		proceed(map, mapfile);

		appendLog();
		appendLog("Map information:");
		appendLog("----------------");

//		String classname = thisJoinPointStaticPart.getSignature().getDeclaringType().getSimpleName();

		appendLog("Name:    " + map.name);
		appendLog("Theater: " + map.theater.label);
		appendLog("Size:    " + map.bounds.width() + "x" + map.bounds.height());
		appendLog();

		appendLog("...done");
		gapMinus();
	}

	/**
	 * Log creation of map/mission parts.
	 */
	before(): if (debug) &&
		initialization(MapPackRA.new(..)) ||
		initialization(OverlayPackRA.new(..)) ||
		initialization(TerrainRA.new(..)) {

		String mappart = thisJoinPointStaticPart.getSignature().getDeclaringType().getSimpleName();
		mappart = mappart.substring(0, mappart.length() - 2);
		appendLog("MapRA: Building " + mappart + "...");
	}

	/**
	 * Pointcut over the creation of a new single player campaign.
	 */
	before():
		initialization(public SinglePlayerCampaign.new(..)) {

		appendLog("Starting new Single-Player campaign.");
	}

	/**
	 * Pointcut over the creation of an object impl.
	 * 
	 * @param subfaction The subfaction this impl will belong to.
	 * @param name		 Name of the implementation.
	 */
	pointcut createImpl(SubFaction subfaction, String name):
		execution(private Implementation ObjectFactory.createImplementation(SubFaction, String)) &&
		args(subfaction, name);

	/**
	 * Log loading of the creation of structure implementations.
	 * 
	 * @param faction The faction this structure will belong to.
	 * @param name	  Name of the structure.
	 */
	@SuppressWarnings("unused")
	before(SubFaction subfaction, String name): if (debug) &&
		createImpl(subfaction, name) && cflow(execution(* UnitFactory.createStructure(..))) {

		appendLog("Creating structure " + name + " for " + subfaction.getID());
	}

	/**
	 * Log loading of the creation of vehicle implementations.
	 * 
	 * @param subfaction The subfaction this vehicle will belong to.
	 * @param name		 Name of the vehicle.
	 */
	@SuppressWarnings("unused")
	before(SubFaction subfaction, String name): if (debug) &&
		createImpl(subfaction, name) && cflow(execution(* UnitFactory.createVehicle(..))) {

		appendLog("Creating vehicle " + name + " for " + subfaction.getID());
	}

	/**
	 * Outputs exception/error messages to the console and to the log file.
	 * 
	 * @param ex The exception that has been caught.
	 */
	before(Throwable ex):
		(handler(Exception) || handler(Error)) && within(Main) && args(ex) {

		appendLog();
		appendLog("An error has occured.  Refer to Debug_Err.log for details");

		// Find the root cause and message
		Throwable cause = ex;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}

		// Print to the error stream
		System.err.println("Exception/Error thrown:");
		System.err.println("-----------------------");
		System.err.println("\t" + (cause.getMessage() != null ? cause.getMessage() : "(none given)"));
		System.err.println();
		System.err.println("Stack trace:");
		System.err.println("------------");
		ex.printStackTrace();
	}

/* *************** *
 * Logging methods *
 * *************** */

	/**
	 * Adds a blank line to the debug log.
	 */
	private static void appendLog() {

		applog.println();
		System.out.println();
	}

	/**
	 * Adds another line to the debug log.
	 * 
	 * @param text String to append to the log on a new line.
	 */
	private static void appendLog(String text) {

		applog.println(gap + text);
		System.out.println(gap + text);
	}

	/**
	 * Closes the log files, and the application and error print streams.
	 */
	private static void closeDebugStreams() {

		applog.close();
		errlog.close();
	}

	/**
	 * Increases the gap size by 1 tab.
	 */
	private static void gapPlus() {

		gap += "\t";
	}

	/**
	 * Decreases the gap size by 1 tab.
	 */
	private static void gapMinus() {

		gap = gap.substring(0, gap.length() - 1);
	}

	/**
	 * Creates the <tt>PrintStream</tt>s used as output files/logs for general
	 * output and error logging.  Redirects the <tt>System.err</tt> and
	 * <tt>System.out</tt> print streams to these files.
	 * <p>
	 * (was made a separate method so that a pointcut can more easily expose it)
	 */
	private static void openDebugStreams() {

		applog = new PrintStream(logfile);
		errlog = new PrintStream(errfile);
//		System.setOut(applog);
		System.setErr(errlog);
	}
}
