package pt.up.fe.unite.mining.labels;

import pt.up.fe.unite.extraction.reader.Reader;

import pt.up.fe.unite.util.Pair;
import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.util.Trackable;
import pt.up.fe.unite.util.ProgressTracker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0406
 * @since 1.6
 */
public abstract class Labeler implements Trackable
{
	public static final String SK_COMMUNITY_FILE = "l-community";
	public static final String SK_CONDITION_FIELD = "l-condition-field";
	public static final String SK_MAX = "l-max";

	public static final long DEFAULT_NOTIFICATION_INTERVAL = 30;
	public static final TimeUnit DEFAULT_NOTIFICATION_TIMEUNIT = TimeUnit.SECONDS;

	protected Logger log;
	protected Reader reader;

	private long notificationInterval;
	private TimeUnit notificationTimeUnit;

	private long rowsProcessed;

	public Labeler( Reader reader )
	{
		this( reader, DEFAULT_NOTIFICATION_INTERVAL, DEFAULT_NOTIFICATION_TIMEUNIT );
	}
	
	public Labeler( Reader reader, long notificationInterval, TimeUnit notificationTimeUnit )
	{
		log = Logging.getLogger( this );
		this.reader = reader;
		this.notificationInterval = notificationInterval;
		this.notificationTimeUnit = notificationTimeUnit;
	}

	public void run()
	{
		rowsProcessed = 0;
		ProgressTracker progressTracker = new ProgressTracker(
				notificationInterval,	notificationTimeUnit, this );

		while ( true )
		{
			Pair<String,String> content = reader.next();
			if ( content == Pair.NULL ) break;
			if ( content == null ) continue;

			parse( content.getLeft() );
			rowsProcessed++;
		}
		
		close();
		progressTracker.stop();
		reader.close();
	}

	public void track()
	{
		log.info( rowsProcessed + " rows processed" );
	}
	
	protected void close() { }
	protected abstract void parse( String content );
	public abstract List<String> getLabels( int maxLabels );
	//public abstract List<String> getNodes();
}
