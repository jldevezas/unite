package com.josedevezas.unite.mining.properties;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Properties;
import java.util.NoSuchElementException;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

import org.apache.commons.lang.ArrayUtils;

/**
 * Set the same property value for the given group of nodes.
 *
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0406
 * @since 1.6
 */
public class CustomPropertyBuilder extends PropertyBuilder
{
	private List<String> nodeKeys;
	private String keyProperty;
	private List<String> values;
	private String valueProperty;
	private Boolean append;

	public CustomPropertyBuilder(
			Properties settings,
			List<String> nodeKeys,
			String keyProperty,
			List<String> values,
			String valueProperty,
			Boolean append )
	{
		super( settings );

		this.nodeKeys = nodeKeys;
		this.keyProperty = keyProperty;
		this.values = values;
		this.valueProperty = valueProperty;
		this.append = append;
	}
	
	public CustomPropertyBuilder(
			Properties settings,
			List<String> nodeKeys,
			String keyProperty,
			List<String> values,
			String valueProperty )
	{
		super( settings );

		this.nodeKeys = nodeKeys;
		this.keyProperty = keyProperty;
		this.values = values;
		this.valueProperty = valueProperty;
		this.append = false;
	}

	public void run()
	{
		long success = 0;

		log.info( "Setting property '" + valueProperty + "' as " +
				values + " for specified nodes" );

		Index<Vertex> index = ( (IndexableGraph) graph )
			.getIndex( Index.VERTICES, Vertex.class );

		for ( String nodeKey : nodeKeys )
		{
			try
			{
				log.debug( "Getting node with " + keyProperty + " = " + nodeKey );
				Vertex vertex = index.get( keyProperty, nodeKey ).iterator().next();

				Set<String> labelSet = new HashSet<String>( values );
				if ( append )
				{
					labelSet.addAll( Arrays.asList(
								(String[]) vertex.getProperty( valueProperty ) ) );
				}

				vertex.setProperty( valueProperty, labelSet.toArray( new String[0] ) );
				success++;
			}
			catch ( NoSuchElementException e )
			{
				log.warn( "Couldn't find node with " + keyProperty +
						" = " + nodeKey + ", skipping" );
			}
		}

		log.info( success + " nodes out of " + nodeKeys.size() + " successfuly labeled" );
	}
}
