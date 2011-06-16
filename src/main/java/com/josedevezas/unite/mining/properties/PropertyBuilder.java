package com.josedevezas.unite.mining.properties;

import com.josedevezas.unite.util.Logging;
import com.josedevezas.unite.mining.properties.PropertyBuilder;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0303
 * @since 1.6
 */
public abstract class PropertyBuilder
{
	public static final String SK_DATABASE = "p-database";
	public static final String SK_IMPLEMENTATION = "p-implementation";
	public static final String SK_PROPERTY = "p-property";
	public static final String SK_KEY_FIELD = "p-node-key";

	public static final String SV_IMPLEMENTATION_NEO4J = "Neo4j";
	public static final String SV_IMPLEMENTATION_ORIENTDB = "OrientDB";
	public static final String SV_PROPERTY_DEGREE = "degree";
	public static final String SV_PROPERTY_CENTRALITY = "centrality";

	public static final Properties DEFAULT_SETTINGS = new Properties()
	{
		{
			setProperty( SK_IMPLEMENTATION, SV_IMPLEMENTATION_NEO4J );
		}
	};

	protected Logger log;
	protected Graph graph;
	protected Properties settings;

	private String dbString;

	public PropertyBuilder( Properties settings )
	{
		log = Logging.getLogger( this );
		graph = null;
		this.settings = settings;

		if ( settings.getProperty( SK_DATABASE ) == null )
			throw new IllegalArgumentException( "Value of SK_DATABASE can't be null" );
		
		if ( settings.getProperty( SK_IMPLEMENTATION ) == null )
			throw new IllegalArgumentException( "Value of SK_IMPLEMENTATION can't be null" );

		if ( settings.getProperty( SK_IMPLEMENTATION ).equals( SV_IMPLEMENTATION_ORIENTDB ) )
		{
			dbString = "local:" + settings.getProperty( SK_DATABASE );
			log.info( "Opening OrientDB database in " + dbString );
			graph = new OrientGraph( dbString );
		}
		else if ( settings.getProperty( SK_IMPLEMENTATION ).equals( SV_IMPLEMENTATION_NEO4J ) )
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
	}

	public void close()
	{
		if ( graph != null )
		{
			log.info( "Closing " + dbString );
			graph.shutdown();
		}
	}

	public abstract void run();
}
