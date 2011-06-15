package pt.up.fe.unite.util;

/**
 * Implementing this makes the progress of a class trackable.
 *
 * When a class is Trackable, it can be monitored by a ProgressTracker
 * that calls on the track method periodically.
 *
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0310
 * @since 1.6
 */
public interface Trackable
{
	/**
	 * Outputs information about the progress of a process.
	 *
	 * The output is likely to be a logger or the System.out.
	 */
	public void track();
}
