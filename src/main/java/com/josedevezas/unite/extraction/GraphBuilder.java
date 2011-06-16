package com.josedevezas.unite.extraction;

import com.josedevezas.unite.util.Pair;
import com.josedevezas.unite.util.Trackable;
import com.josedevezas.unite.util.ProgressTracker;
import com.josedevezas.unite.extraction.util.PairProcessor;
import com.josedevezas.unite.extraction.reader.Reader;
import com.josedevezas.unite.extraction.writer.Writer;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class GraphBuilder implements Trackable
{
	public static final int DEFAULT_RESULTS_QUEUE_CAPACITY = 1000;
	public static final int DEFAULT_THREADS_PER_PROCESSOR = 50;
	public static final long DEFAULT_NOTIFICATION_INTERVAL = 30;
	public static final TimeUnit DEFAULT_NOTIFICATION_TIMEUNIT = TimeUnit.SECONDS;

	private int threadsPerProcessor;
	private long notificationInterval;
	private TimeUnit notificationTimeUnit;

	private long processedRows;

	private Logger log;
	private LinkedBlockingQueue<Pair<String,String>> resultQueue;
	private Reader reader;
	private Pair<Class,Class> parser;
	private Writer writer;

	/**
	 * Initializes a GraphBuilder with the default parameters.
	 *
	 * @param reader an initialized instance of one of Reader subclasses.
	 * @param parser a Pair of Parser subclasses to parse the source and target content.
	 * @param writer an initialized instance of one of Writer subclasses.
	 */
	public GraphBuilder( Reader reader, Pair<Class,Class> parser, Writer writer )
	{
		this( reader, parser, writer, DEFAULT_RESULTS_QUEUE_CAPACITY,
				DEFAULT_THREADS_PER_PROCESSOR, DEFAULT_NOTIFICATION_INTERVAL,
				DEFAULT_NOTIFICATION_TIMEUNIT	);
	}

	/**
	 * Initializes a GraphBuilder with the given parameters.
	 *
	 * @param reader an initialized instance of one of Reader subclasses.
	 * @param parser a Pair of Parser subclasses to parse the source and target content.
	 * @param writer an initialized instance of one of Writer subclasses.
	 * @param resultsQueueCapacity the limit of edges to keep in memory.
	 * @param threadsPerProcessor the limit of threads of PairProcessors.
	 * @param notificationInterval the interval at which to track the progress.
	 * @param notificationTimeUnit the TimeUnit for the tracking interval.
	 */
	public GraphBuilder(
			Reader reader,
			Pair<Class,Class> parser,
			Writer writer ,
			int resultsQueueCapacity,
			int threadsPerProcessor,
			long notificationInterval,
			TimeUnit notificationTimeUnit )
	{
		this.log = Logger.getLogger( this.getClass().getName() );
		this.resultQueue = new LinkedBlockingQueue<Pair<String,String>>( resultsQueueCapacity );
		this.threadsPerProcessor = threadsPerProcessor;

		this.notificationInterval = notificationInterval;
		this.notificationTimeUnit = notificationTimeUnit;

		this.reader = reader;
		this.parser = parser;
		this.writer = writer;
	}

	private void shutdownAndAwaitTermination( ExecutorService pool ) {
		log.info( "Finishing pending tasks" );
		pool.shutdown();

		try
		{
			if ( !pool.awaitTermination( 60, TimeUnit.SECONDS ) )
			{
				log.info( "Pending tasks timeout, forcing shutdown" );
				pool.shutdownNow();

				if ( !pool.awaitTermination( 60, TimeUnit.SECONDS ) )
					log.error( "Thread pool did not terminate" );
			}
		}
		catch ( InterruptedException ie ) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}	

	/**
	 * Build the graph.
	 *
	 * A GraphBuilder will use a Reader to get source and target content to be parsed.
	 * Source content is where the source nodes will be found and target content,
	 * similarly, is where the target nodes will be found. Both the source and target
	 * content will be parsed by its own parser and converted into Pairs of source
	 * and target nodes, or edges. Theses edges will be then written to a persistent
	 * resource, such as tab separated text file or a graph database, using one of the
	 * Writer class implementations.
	 */
	public void run()
	{
		int poolSize = Runtime.getRuntime().availableProcessors() * threadsPerProcessor;
		ThreadPoolExecutor pool = new ThreadPoolExecutor( poolSize, poolSize,
				0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>( poolSize ),
				new ThreadPoolExecutor.CallerRunsPolicy() );

		processedRows = 0;
		ProgressTracker progressTracker = new ProgressTracker( notificationInterval,
				notificationTimeUnit, this );

		writer.setResultQueue( resultQueue );
		writer.start();

		try
		{
			log.info( "Parsing and writing results" );
			while ( true )
			{
				log.trace( "Asking reader for next content pair" );
				Pair<String,String> content = reader.next();

				if ( content == null ) continue;
				if ( content == Pair.NULL ) break;

				log.trace( "Launching new PairProcessor" );
				pool.execute( new PairProcessor( content, parser, resultQueue ) );
				processedRows++;
			}
			
			progressTracker.stop();
			shutdownAndAwaitTermination( pool );
			reader.close();

			log.trace( "Putting null pair in results queue" );
			resultQueue.put( Pair.NULL );

			writer.join();
			writer.close();
		}
		catch ( InterruptedException ie )
		{
			log.error( ie.getMessage() );
		}
	}

	/**
	 * A tracker for the number of rows processed so far.
	 *
	 * In this context, a row refers to a content Pair.
	 */
	public void track()
	{
		log.info( processedRows + " rows processed so far" );
	}
}
