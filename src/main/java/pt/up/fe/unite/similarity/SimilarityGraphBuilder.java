package pt.up.fe.unite.similarity;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.util.Trackable;
import pt.up.fe.unite.util.ProgressTracker;
import pt.up.fe.unite.similarity.util.EntitiySimilarity;
import pt.up.fe.unite.similarity.reader.DescriptorReader;
import pt.up.fe.unite.similarity.writer.SimilarityGraphWriter;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Builds a graph from data descriptors, based on a similarity metric.
 *
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0419
 * @since 1.6
 */
public class SimilarityGraphBuilder implements Trackable
{
	public static final long DEFAULT_NOTIFICATION_INTERVAL = 30;
	public static final TimeUnit DEFAULT_NOTIFICATION_TIMEUNIT = TimeUnit.SECONDS;

	private long notificationInterval;
	private TimeUnit notificationTimeUnit;

	private long writtenEdges;
	private Logger log;

	private DescriptorReader reader;
	private SimilarityGraphWriter writer;

	public SimilarityGraphBuilder( DescriptorReader reader, SimilarityGraphWriter writer )
	{
		this.log = Logging.getLogger( this );
		this.writtenEdges = 0;

		this.notificationInterval = DEFAULT_NOTIFICATION_INTERVAL;
		this.notificationTimeUnit = DEFAULT_NOTIFICATION_TIMEUNIT;

		this.reader = reader;
		this.writer = writer;
	}

	public void run()
	{
		List<Double> descriptor1 = null;
		List<Double> descriptor2 = null;

		int maxNeighbors = 10;

		log.info( "Calculating similarities and building the graph" );

		ProgressTracker progressTracker = new ProgressTracker( notificationInterval,
				notificationTimeUnit, this );

		try
		{
			while ( ( descriptor1 = reader.next() ) != null )
			{
				String id1 = reader.getId();

				DescriptorReader reader2 = reader.clone();

				PriorityQueue<EntitiySimilarity> queue =
					new PriorityQueue<EntitiySimilarity>( maxNeighbors );

				while ( ( descriptor2 = reader2.next() ) != null )
				{
					String id2 = reader2.getId();

					if ( id1.equals( id2 ) ) continue;

					if ( queue.size() >= maxNeighbors ) queue.poll();

					queue.add( new EntitiySimilarity( id2,
								Metrics.euclideanDistance(
									descriptor1.toArray( new Double[0] ),
									descriptor2.toArray( new Double[0] ) ) ) );
				}
				
				writer.addNeighbors( id1, queue );

				writtenEdges += maxNeighbors;
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			log.error( e );
		}

		progressTracker.stop();
		writer.close();

		log.info( "Similarity graph completed" );
	}

	public void track()
	{
		log.info( writtenEdges + " edges written so far" );
	}
}
