package com.josedevezas.unite.mining.stats

import com.tinkerpop.gremlin.Gremlin
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph

class CommunityStats
{
	public static final String SK_DATABASE = "stats-database"
	public static final String SK_COMMUNITY_PROPERTY = "stats-community-property"
	public static final String SK_FORMAT = "stats-format"
	public static final String SK_OUT_FILE = "stats-out-file"
	public static final String SK_TYPE = "stats-type"
	public static final String SK_NODE_MATCH = "stats-node-filter"

	private String graphDbPath
	private String communityProperty
	private String format
	private OutputStream out
	private String nodeMatch

	static
	{
		Gremlin.load()
	}

	CommunityStats( String graphDbPath, String communityProperty, String nodeMatch=".*",
		String format="plain", OutputStream out=System.out )
	{
		this.graphDbPath = graphDbPath
		this.communityProperty = communityProperty
		this.format = format
		this.out = out
		this.nodeMatch = nodeMatch
	}

	private void writeHelper( def map, String keyTitle, String valueTitle )
	{
		switch ( format )
		{
			case "tab":
				out.write( ( keyTitle + "\t" + valueTitle + "\n" ).getBytes() )
				map.each{ k,v -> out.write( ( k + '\t' + v + '\n' ).getBytes() ) }
				out.close()
				break
			case "plain":
			default:
				out.write( map.toString().getBytes() )
				out.write( '\n' )
				out.close()
				break
		}
	}

	void nodesPerCommunity()
	{
		def g = new Neo4jGraph( graphDbPath )
		def m = [:]

		g.V{ it.communities != null & it.url.matches( nodeMatch ) }.communities.each{ l ->
			l.each{ c ->
				if ( m[ c ] == null )
					m[ c ] = 1
				else
					m[ c ]++
			}
		}

		g.shutdown()
		writeHelper( m, "community", "nodes" )
	}

	void nodeOverlap()
	{
		def g = new Neo4jGraph( graphDbPath )
		def m = [:]
		
		g.V.each{
			if ( it.communities == null )
				m[ it.id ] = 0
			else
			{
				def counter = 0
				it.communities.each{ c -> counter++ }
				m[ it.id ] = counter
			}
		}

		g.shutdown()
		writeHelper( m, "node", "overlap" )
	}
}
