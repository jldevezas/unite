package com.josedevezas.unite.mining.properties;

import java.util.Properties;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.Edge;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0303
 * @since 1.6
 */
public class DegreeBuilder extends PropertyBuilder
{
	public static final String SK_IN_NAME = "inName";
	public static final String SK_OUT_NAME = "outName";
	public static final String SK_BOTH_NAME = "bothName";
	public static final String SK_TYPE = "p-type";

	public static final String SV_TYPE_IN = "in";
	public static final String SV_TYPE_OUT = "out";
	public static final String SV_TYPE_BOTH = "both";
	public static final String SV_TYPE_ALL = "all";

	public static final String SV_IN_NAME_DEFAULT = "indegree";
	public static final String SV_OUT_NAME_DEFAULT = "outdegree";
	public static final String SV_BOTH_NAME_DEFAULT = "degree";

	public static final Properties DEFAULT_SETTINGS =
		new Properties( PropertyBuilder.DEFAULT_SETTINGS )
	{
		{
			setProperty( SK_TYPE, SV_TYPE_ALL );
		}
	};

	private boolean cIn, cOut, cBoth;

	public DegreeBuilder( Properties settings )
	{
		super( settings );

		if ( settings.getProperty( SK_TYPE ) == null )
			throw new IllegalArgumentException( "Value of SK_TYPE can't be null" );

		if ( ! ( settings.getProperty( SK_TYPE ).equals( SV_TYPE_ALL )
					|| settings.getProperty( SK_TYPE ).equals( SV_TYPE_IN )
					|| settings.getProperty( SK_TYPE ).equals( SV_TYPE_OUT )
					|| settings.getProperty( SK_TYPE ).equals( SV_TYPE_BOTH ) ) )
			throw new IllegalArgumentException( "Value of SK_TYPE must either be "
					+	" SV_TYPE_IN, SV_TYPE_OUT, SV_TYPE_BOTH or SV_TYPE_ALL" );

		cIn = cOut = cBoth = false;
		if ( settings.getProperty( SK_TYPE ).equals( SV_TYPE_ALL ) )
			cIn = cOut = cBoth = true;
		else if ( settings.getProperty( SK_TYPE ).equals( SV_TYPE_IN ) )
			cIn = true;
		else if ( settings.getProperty( SK_TYPE ).equals( SV_TYPE_OUT ) )
			cOut = true;
		else if ( settings.getProperty( SK_TYPE ).equals( SV_TYPE_BOTH ) )
			cBoth = true;
	}

	public void run()
	{
		for ( Vertex vertex : graph.getVertices() )
		{
			long degree = 0;
			long inDegree = 0;
			long outDegree = 0;

			if ( cIn || cBoth )
			{
				for ( Edge edge : vertex.getInEdges() ) inDegree++;

				vertex.setProperty(
						settings.getProperty( SK_IN_NAME, SV_IN_NAME_DEFAULT ), inDegree );
			}

			if ( cOut || cBoth )
			{
				for ( Edge edge: vertex.getOutEdges() ) outDegree++;
				
				vertex.setProperty(
						settings.getProperty( SK_OUT_NAME, SV_OUT_NAME_DEFAULT ), outDegree );
			}

			if ( cBoth )
			{
				degree = inDegree + outDegree;
				
				vertex.setProperty(
						settings.getProperty( SK_BOTH_NAME, SV_BOTH_NAME_DEFAULT ), degree );
			}
		}
	}
}
