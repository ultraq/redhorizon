
// ===================================================
// Scanner's AspectJ - Data stream exception softening
// ===================================================

package redhorizon.utilities;

import java.util.concurrent.BlockingQueue;

/**
 * Softens the exceptions within the {@link DataStream} classes.
 * 
 * @author Emanuel Rabina
 */
public aspect DataStreamSoftener {

	/**
	 * Soften interruption exceptions on the blocking queue methods.
	 */
	pointcut blockQueue():
		call(public void BlockingQueue.put(..)) || call (public * BlockingQueue.take());

	declare soft: InterruptedException: blockQueue();
}
