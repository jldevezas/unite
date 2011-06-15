package pt.up.fe.unite.mining.communities;

import pt.up.fe.unite.util.ProgressTracker;
import pt.up.fe.unite.util.Trackable;
import pt.up.fe.unite.util.Logging;

import java.util.HashSet;
import java.util.Iterator;

import java.util.concurrent.TimeUnit;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.Edge;

import org.apache.log4j.Logger;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0309
 * @since 1.6
 */
public class CliquePercolation implements Trackable
{
	public static final long DEFAULT_NOTIFICATION_INTERVAL = 30;
	public static final TimeUnit DEFAULT_NOTIFICATION_TIMEUNIT = TimeUnit.SECONDS;

	private Logger log;
	private HashSet<HashSet<Vertex>> cliques;

	private long notificationInterval;
	private TimeUnit notificationTimeUnit;

	private ProgressTracker progressTracker;

	private long cliquesIdentified;
	private long recursiveCalls;
	private long loopsRan;

	public CliquePercolation()
	{
		this( DEFAULT_NOTIFICATION_INTERVAL, DEFAULT_NOTIFICATION_TIMEUNIT );
	}

	public CliquePercolation( long notificationInterval, TimeUnit notificationTimeUnit )
	{
		log = Logging.getLogger( this );
		cliques = new HashSet<HashSet<Vertex>>();

		cliquesIdentified = 0;
		recursiveCalls = 0;
		loopsRan = 0;

		this.notificationInterval = notificationInterval;
		this.notificationTimeUnit = notificationTimeUnit;
		
		progressTracker = new ProgressTracker( notificationInterval,
				notificationTimeUnit, this );
	}

	public HashSet<HashSet<Vertex>> getResultSet()
	{
		return this.cliques;
	}

	// This is too slow for large-scale, needs to be optimized.
	public void bronKerbosch(
			HashSet<Vertex> rSet,
			HashSet<Vertex> pSet,
			HashSet<Vertex> xSet )			
	{
		log.trace( "Running Bron-Kerbosch for R(" + rSet.size() + "), P("
				+ pSet.size() + "), X(" + xSet.size() + ")" );
		recursiveCalls++;

		if ( pSet.isEmpty() && xSet.isEmpty() )
		{
			if ( rSet.size() < 3 ) return;

			log.debug( "Found maximal clique: " + rSet );
			for ( Vertex vertex : rSet )
				System.out.println( vertex.getProperty( "url" ) );
			cliques.add( new HashSet( rSet ) );
			cliquesIdentified++;
			return;
		}

		Iterator<Vertex> iterator = pSet.iterator();
		while ( iterator.hasNext() )
		{
			loopsRan++;
			Vertex vertex = iterator.next();

			HashSet<Vertex> neighborSet = new HashSet<Vertex>();

			for ( Edge edge : vertex.getOutEdges() )
				neighborSet.add( edge.getInVertex() );
			
			for ( Edge edge : vertex.getInEdges() )
				neighborSet.add( edge.getOutVertex() );

			HashSet<Vertex> nextRSet = new HashSet<Vertex>( rSet );
			nextRSet.add( vertex );

			HashSet<Vertex> nextPSet = new HashSet<Vertex>( pSet );
			nextPSet.retainAll( neighborSet );

			HashSet<Vertex> nextXSet = new HashSet<Vertex>( xSet );
			nextXSet.retainAll( neighborSet );

			bronKerbosch( nextRSet, nextPSet, nextXSet );

			iterator.remove();
			xSet.add( vertex );
		}
	}

	public void track()
	{
		log.info( cliquesIdentified + " maximal cliques identified, with "
			 + recursiveCalls + " recursive calls and " + loopsRan + " loops" );
	}

	public void shutdown()
	{
		progressTracker.stop();
	}
}
