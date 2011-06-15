package pt.up.fe.unite.extraction.writer;

import java.util.Properties;
import java.util.NoSuchElementException;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper.CommitManager;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class BlueprintsWriter extends Writer
{
	private static final int DEFAULT_COMMIT_INTERVAL = 10000;

	private Graph graph;
	private Properties settings;
	private String dbString;
	
	private Index<Vertex> vertexIndex;
	private Index<Edge> edgeIndex;
	private Vertex sourceVertex;
	private Vertex targetVertex;
	private Edge edge;
	private CommitManager manager;

	public BlueprintsWriter( Properties settings )
		throws IllegalArgumentException
	{
		super();

		this.settings = settings;

		/*
		 * Check if all necessary arguments are set.
		 */
		if ( settings.getProperty( SK_IMPLEMENTATION ) == null )
			throw new IllegalArgumentException( "Value of SK_IMPLEMENTATION can't be null" );
		
		if ( settings.getProperty( SK_DATABASE ) == null )
			throw new IllegalArgumentException( "Value of SK_DATABASE can't be null" );

		if ( settings.getProperty( SK_RELATIONSHIP ) == null )
			throw new IllegalArgumentException( "Value of SK_RELATIONSHIP can't be null" );

		if ( settings.getProperty( SK_SOURCE_NAME ) == null )
			throw new IllegalArgumentException( "Value of SK_SOURCE_NAME can't be null" );

		if ( settings.getProperty( SK_TARGET_NAME ) == null )
			throw new IllegalArgumentException( "Value of SK_TARGET_NAME can't be null" );
		/*/
		 * End of block.
		 */

		if ( settings.getProperty( SK_IMPLEMENTATION ).equals( "OrientDB" ) )
		{
			dbString = "local:" + settings.getProperty( SK_DATABASE );
			log.info( "Opening OrientDB database in " + dbString );
			graph = new OrientGraph( dbString );
		}
		else if ( settings.getProperty( SK_IMPLEMENTATION ).equals( "Neo4j" ) )
		{
			dbString = settings.getProperty( SK_DATABASE );
			log.info( "Opening Neo4j database in " + dbString );
			graph = new Neo4jGraph( dbString );
		}
		else
		{
			throw new IllegalArgumentException( "No graph database implementation of "
					+ settings.getProperty( SK_IMPLEMENTATION ) );
		}

		this.edgeIndex = ( (IndexableGraph) graph ).getIndex( Index.EDGES, Edge.class );
		this.vertexIndex = ( (IndexableGraph) graph ).getIndex( Index.VERTICES, Vertex.class );
		this.manager = null;
	}

	public void write( String source, String target )
	{
		// TODO not the best to call this, but transactions need to begin inside the
		// corresponding method.
		if ( manager == null )
			manager = TransactionalGraphHelper.createCommitManager(
					(TransactionalGraph) graph, DEFAULT_COMMIT_INTERVAL );

		if ( source.equals( target ) )
		{
			log.debug( "Source and target nodes are the same, skipping" );
			return;
		}

		log.trace( "Building " + source + " -> " + target );
		
		try
		{
			sourceVertex = vertexIndex
				.get( settings.getProperty( SK_SOURCE_NAME ), source )
				.iterator().next();
		}
		catch ( NoSuchElementException ne )
		{
			log.trace( "Source vertex not found, creating" );
			sourceVertex = graph.addVertex( null );
			sourceVertex.setProperty( settings.getProperty( SK_SOURCE_NAME ), source );
		}

		try
		{
			targetVertex = vertexIndex
				.get( settings.getProperty( SK_TARGET_NAME ), target )
				.iterator().next();
		}
		catch ( NoSuchElementException ne )
		{
			log.trace( "Target vertex not found, creating" );
			targetVertex = graph.addVertex( null );
			targetVertex.setProperty( settings.getProperty( SK_TARGET_NAME ), target );
		}
		
		String edgeId = ( (Long) sourceVertex.getId() ).toString()
			+ "->" + ( (Long) targetVertex.getId() ).toString();
		log.trace( "ID for the current edge is " + edgeId );

		try
		{
			log.trace( "Querying index for " + edgeId );
			edge = edgeIndex
				.get( settings.getProperty( SK_EDGE_ID_NAME ), edgeId )
				.iterator().next();
			Long weight = (Long) edge.getProperty( settings.getProperty( SK_EDGE_WEIGHT_NAME ) );
			log.trace( "Current weight is " + weight );
			edge.setProperty( settings.getProperty( SK_EDGE_WEIGHT_NAME ), weight );
		}
		catch ( NoSuchElementException ne )
		{
			log.trace( "Edge not found, creating" );
			edge = graph.addEdge( null, sourceVertex, targetVertex,
					settings.getProperty( SK_RELATIONSHIP ) );
			edge.setProperty( settings.getProperty( SK_EDGE_ID_NAME ), edgeId );
			edge.setProperty( settings.getProperty( SK_EDGE_WEIGHT_NAME ), new Long( 1 ) );
			manager.incrCounter();
		}

		if ( manager.atCommit() )
		{
			log.debug( "Commiting transaction" );
			System.gc();
		}
	}

	public void close()
	{
		if ( manager != null )
		{
			log.debug( "Closing commit manager" );
			manager.close();
		}

		if ( graph != null )
		{
			log.info( "Closing " + dbString );
			graph.shutdown();
		}
	}
}
