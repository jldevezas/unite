package com.josedevezas.unite.extraction.writer;

import com.josedevezas.unite.util.Pair;
import com.josedevezas.unite.util.Logging;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * This class Writer provides a basic framework to implement an edge writer.
 *
 * Subclasses should implement their own write method, making their own
 * constructors as needed, for instance to receive a Properties with
 * configuration settings for the Writer, and optionally implementing
 * a close method if necessary.
 *
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public abstract class Writer extends Thread
{
	public static final String SK_IMPLEMENTATION		= "w-implementation";
	public static final String SK_DATABASE					= "w-database";
	public static final String SK_RELATIONSHIP			= "w-relationship";
	public static final String SK_SOURCE_NAME				= "w-source-name";
	public static final String SK_TARGET_NAME				= "w-target-name";
	public static final String SK_EDGE_ID_NAME			= "w-edge-id-name";
	public static final String SK_EDGE_WEIGHT_NAME	= "w-edge-weight-name";

	public static final String SV_IMPLEMENTATION_NEO4J = "Neo4j";
	public static final String SV_IMPLEMENTATION_ORIENTDB = "OrientDB";

	public static final Properties DEFAULT_SETTINGS = new Properties()
	{
		{
			setProperty( SK_IMPLEMENTATION, SV_IMPLEMENTATION_NEO4J );
			setProperty( SK_RELATIONSHIP, "links-to" );
			setProperty( SK_SOURCE_NAME, "name" );
			setProperty( SK_TARGET_NAME, "name" );
			setProperty( SK_EDGE_ID_NAME, "id" );
			setProperty( SK_EDGE_WEIGHT_NAME, "weight" );
		}
	};

	private static final long RETRY_INTERVAL = 5;
	private static final TimeUnit RETRY_TIMEUNIT = TimeUnit.SECONDS;

	protected Logger log;
	protected LinkedBlockingQueue<Pair<String,String>> resultQueue;

	/**
	 * Creates a new writer with its own logger.
	 *
	 * The logger is an instance of Logger, from the Log4j package, that
	 * should be used in any subclasses of Writer to log the exceptions.
	 */
	public Writer()
	{
		log = Logging.getLogger( this );
	}

	/**
	 * Sets the queue that will be used to poll and write data.
	 *
	 * The queue defined here is continuously monitored for source/target pairs,
	 * writing them as source node points to target node, for the given implementation
	 * of Writer.
	 *
	 * @param resultQueue a LinkedBlockingQueue containing Pairs of Strings (the left
	 * one representing the source node and the right one the target node of a graph).
	 */
	public void setResultQueue( LinkedBlockingQueue<Pair<String,String>> resultQueue )
	{
		this.resultQueue = resultQueue;
	}

	/**
	 * Monitors the result queue for new elements and writes them.
	 *
	 * Each element is written using and implementation of the method write that
	 * should be implemented in subclasses. The queue is polled frequently, blocking
	 * for the interval of time defined by RETRY_INTERVAL and RETRY_TIMEUNIT. Loop
	 * ends when a Pair.NULL is found in the queue.
	 */
	public void run()
	{
		try
		{
			while ( true )
			{
				log.trace( "Polling queue(" + resultQueue.size() +  ") for result" );
				Pair<String,String> edge = resultQueue.poll( RETRY_INTERVAL, RETRY_TIMEUNIT );

				if ( edge == null )
				{
					log.warn( "Queue empty for " + RETRY_INTERVAL + " seconds, retrying" );
					continue;
				}

				if ( edge.getLeft() == null || edge.getRight() == null ) break;

				write( edge.getLeft(), edge.getRight() );
			}
		}
		catch ( InterruptedException ie )
		{
			log.warn( ie.getMessage() );
		}
	}

	/**
	 * Writes an edge to a persistent resource.
	 *
	 * This method could write a source/target pair to a text file, a
	 * graph database or any other resource implemented by a subclass
	 * of Writer.
	 *
	 * @param source a String identifying a source node.
	 * @param target a String identifying a target node.
	 */
	public abstract void write( String source, String target );

	/**
	 * Performs the close tasks required by the Writer.
	 *
	 * Closes file descriptors, databases and any other resources used by
	 * the writer.
	 */
	public abstract void close();
}
