
// ========================================
// Scanner's AspectJ - Preferences debugger
// ========================================

package redhorizon.logging;

import nz.net.ultraq.common.preferences.Preferences;

/**
 * Aspect which is load-time weaved into my implementattion of the Preferences
 * API so that logging can be done on it.
 * 
 * @author Emanuel Rabina
 */
public aspect LogPreferences {

	/**
	 * Log the initialization of the Preferences (alternate entrypoint) class.
	 */
	void around():
		staticinitialization(Preferences) {

		System.out.println("Loading Preferences API implementation...");
		proceed();
		System.out.println("...done");
	}
}
