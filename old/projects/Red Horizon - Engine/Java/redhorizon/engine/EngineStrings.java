
// ====================================
// Scanner's Java - Engine strings keys
// ====================================

package redhorizon.engine;

import redhorizon.strings.StringsKey;

/**
 * Enumeration of keys to text strings used by the game engine.
 * 
 * @author Emanuel Rabina
 */
public enum EngineStrings implements StringsKey {

	// Capabilities checks
	OPENAL_EXTENSION_NOT_FOUND,
	OPENAL_VERSION_NOT_MET,
	OPENGL_EXTENSION_NOT_FOUND,
	OPENGL_VERSION_NOT_MET,

	// Audio engine
	AUDIO_CONTEXT_CREATE,
	AUDIO_CONTEXT_CURRENT,
	AUDIO_CONTEXT_RELEASE,
	AUDIO_DEVICE_CLOSE,
	AUDIO_DEVICE_OPEN,

	// Graphics engine
	GRAPHICS_CONTEXT_CREATE,
	GRAPHICS_CONTEXT_CURRENT,
	GRAPHICS_CONTEXT_RELEASE;

	private static final String RESOURCEBUNDLE_ENGINE = "Game-Engine";

	/**
	 * @inheritDoc
	 */
	public String getKey() {

		return name();
	}

	/**
	 * @inheritDoc
	 */
	public String getResourceBundle() {

		return RESOURCEBUNDLE_ENGINE;
	}
}
