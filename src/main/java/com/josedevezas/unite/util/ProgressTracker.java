package com.josedevezas.unite.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Tracks a class progress at a fixed time interval.
 *
 * This class builds a timer that calls on the track method
 * from a Trackable class periodically, at the given interval.
 *
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0310
 * @since 1.6
 */
public class ProgressTracker
{
	private Timer timer;
	private long msInterval;
	private Trackable object;

	class ProgressTrackerTask extends TimerTask
	{
		public void run()
		{
			object.track();
		}
	}	

	/**
	 * Creates and starts a ProgressTracker that calls track at a fixed interval.
	 *
	 * ProgressTracker calls track in intervals of interval unit, for object.
	 *
	 * @param interval a value representing the repeat time interval.
	 * @param unit the TimeUnit for the repeat time interval.
	 * @param object the object whose progress to track.
	 */
	public ProgressTracker( long interval, TimeUnit unit, Trackable object )
	{
		this.msInterval = TimeUnit.MILLISECONDS.convert( interval, unit );
		this.object = object;

		timer = new Timer();
		timer.scheduleAtFixedRate( new ProgressTrackerTask(), msInterval, msInterval );
	}

	/**
	 * Stops the ProgressTracker.
	 *
	 * This should be called either when the process has finished or
	 * when it is no longer desirable to track the progress.
	 */
	public void stop()
	{
		timer.cancel();
	}
}
