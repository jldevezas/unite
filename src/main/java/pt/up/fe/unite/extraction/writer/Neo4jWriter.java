package pt.up.fe.unite.extraction.writer;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.NoSuchElementException;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.index.BatchInserterIndex;
import org.neo4j.graphdb.index.BatchInserterIndexProvider;

import org.neo4j.kernel.impl.batchinsert.BatchInserter;
import org.neo4j.kernel.impl.batchinsert.BatchInserterImpl;

import org.neo4j.index.impl.lucene.LuceneBatchInserterIndexProvider;

import org.neo4j.helpers.collection.MapUtil;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class Neo4jWriter extends Writer
{
	// TODO class has been changed without re-test, should test!
	private Properties settings;
	private String dbString;

	private BatchInserter inserter;
	private BatchInserterIndexProvider indexProvider;
	private BatchInserterIndex index;

	private Map<String,Object> nodeProperties;
	private Map<String,String> neo4jProperties;
	
	public Neo4jWriter( Properties settings )
	{
		super();

		this.settings = settings;
		this.nodeProperties = new HashMap<String,Object>();
		
		/*
		 * Check if all necessary arguments are set.
		 */
		if ( settings.getProperty( SK_DATABASE ) == null )
			throw new IllegalArgumentException( "Value of SK_DATABASE can't be null" );

		if ( settings.getProperty( SK_RELATIONSHIP ) == null )
			throw new IllegalArgumentException( "Value of SK_RELATIONSHIP can't be null" );

		if ( settings.getProperty( SK_SOURCE_NAME ) == null )
			throw new IllegalArgumentException( "Value of SK_SOURCE_NAME can't be null" );

		if ( settings.getProperty( SK_TARGET_NAME ) == null )
			throw new IllegalArgumentException( "Value of SK_TARGET_NAME can't be null" );

		/*neo4jProperties = new HashMap<String,String>();
		neo4jProperties.put( "neostore.nodestore.db.mapped_memory", "400M" );
		neo4jProperties.put( "neostore.relationshipstore.db.mapped_memory", "400M" );
		neo4jProperties.put( "neostore.propertystore.db.mapped_memory", "100M" );
		neo4jProperties.put( "neostore.propertystore.db.strings.mapped_memory", "100M" );
		neo4jProperties.put( "neostore.propertystore.db.arrays.mapped_memory", "0M" );*/

		dbString = settings.getProperty( SK_DATABASE );

		log.info( "Opening " + dbString );
		inserter = new BatchInserterImpl( dbString/*, neo4jProperties*/ );
		indexProvider = new LuceneBatchInserterIndexProvider( inserter );
		index = indexProvider.nodeIndex( settings.getProperty( SK_SOURCE_NAME ),
				MapUtil.stringMap( "type", "exact" ) );
	}

	public void write( String source, String target )
	{
		log.trace( "Building " + source + " -> " + target );

		long sourceNode;
		long targetNode;

		try
		{
			sourceNode = index.get( settings.getProperty( SK_SOURCE_NAME ), source )
				.iterator().next();
		}
		catch ( NoSuchElementException e )
		{
			log.trace( "Source vertex not found, creating" );
			nodeProperties.put( settings.getProperty( SK_SOURCE_NAME ), source );
			sourceNode = inserter.createNode( nodeProperties );
			index.add( sourceNode, nodeProperties );
		}

		try
		{
			targetNode = index.get( settings.getProperty( SK_TARGET_NAME ), target )
				.iterator().next();
		}
		catch ( NoSuchElementException e )
		{
			log.trace( "Source vertex not found, creating" );
			nodeProperties.put( settings.getProperty( SK_TARGET_NAME ), target );
			targetNode = inserter.createNode( nodeProperties );
			index.add( targetNode, nodeProperties );
		}

		if ( sourceNode == targetNode )
		{
			log.debug( "Source and target nodes are the same, skipping" );
			return;
		}

		log.trace( "Writing edge" );
		inserter.createRelationship( sourceNode, targetNode,
				DynamicRelationshipType.withName( settings.getProperty( SK_RELATIONSHIP ) ), null );
	}

	public void close()
	{
		log.info( "Closing " + dbString );
		indexProvider.shutdown();
		inserter.shutdown();
	}
}
