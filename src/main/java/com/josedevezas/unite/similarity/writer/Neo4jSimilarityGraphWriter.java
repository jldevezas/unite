package com.josedevezas.unite.similarity.writer;

import com.josedevezas.unite.similarity.util.EntitiySimilarity;

import java.util.Collection;
import java.util.NoSuchElementException;

import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

public class Neo4jSimilarityGraphWriter extends SimilarityGraphWriter
{
	public static final String DEFAULT_NODE_ID_PROPERTY_NAME = "name";
	public static final String DEFAULT_EDGE_WEIGHT_PROPERTY_NAME = "weight";
	public static final String DEFAULT_EDGE_TYPE = "is_similar_to";

	private String graphDbPath;

	private Neo4jGraph graph;
	private Index<Vertex> index;

	public Neo4jSimilarityGraphWriter( String graphDbPath )
	{
		super();

		this.graphDbPath = graphDbPath;

		log.info( "Opening Neo4j database in " + graphDbPath ); 
		graph = new Neo4jGraph( graphDbPath );

		index = ( (IndexableGraph) graph ).getIndex( Index.VERTICES, Vertex.class );
	}

	public void addNeighbors( String source, Collection<EntitiySimilarity> targets )
	{
		Vertex sourceVertex = null;

		try
		{
			sourceVertex = index.get( DEFAULT_NODE_ID_PROPERTY_NAME, source ).iterator().next();
		}
		catch ( NoSuchElementException e )
		{
			log.trace( "Source vertex not found, creating" );
			sourceVertex = graph.addVertex( null );
			sourceVertex.setProperty( DEFAULT_NODE_ID_PROPERTY_NAME, source );
		}

		Vertex targetVertex = null;

		for ( EntitiySimilarity entitySimilarity : targets )
		{
			try
			{
				targetVertex = index.get(
						DEFAULT_NODE_ID_PROPERTY_NAME,
						entitySimilarity.getEntity() )
					.iterator().next();
			}
			catch ( NoSuchElementException e )
			{
				log.trace( "Source vertex not found, creating" );
				targetVertex = graph.addVertex( null );
				targetVertex.setProperty(
						DEFAULT_NODE_ID_PROPERTY_NAME,
						entitySimilarity.getEntity() );
			}

			Edge edge = graph.addEdge( null, sourceVertex, targetVertex, DEFAULT_EDGE_TYPE );
			edge.setProperty(
					DEFAULT_EDGE_WEIGHT_PROPERTY_NAME,
					entitySimilarity.getSimilarity() );
		}
	}

	public void close()
	{
		if ( graph != null )
		{
			log.info( "Closing " + graphDbPath );
			graph.shutdown();
		}
	}
}
