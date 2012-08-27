
package redhorizon.filetypes;

import java.nio.channels.GatheringByteChannel;

/**
 * Interface for files that can be written.
 * 
 * @author Emanuel Rabina
 */
public interface WritableFile extends File {

	/**
	 * Writes the complete file to the given byte channel.
	 * 
	 * @param outputchannel File destination.
	 */
	public void write(GatheringByteChannel outputchannel);
}
