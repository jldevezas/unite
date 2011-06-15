package pt.up.fe.unite.application.service;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.application.service.result.NodeRankResult;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.util.LoggerUtils;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.json.JSONConfiguration;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.commons.lang.ClassUtils;

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.Index;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.graphdb.Node;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0414
 * @since 1.6
 */
@Path( "/rank" )
public class NodeRank 
{
	private ArrayList<String> result;

	public NodeRank() {}

	@GET @Produces( { "application/json", "application/xml" } )
	public NodeRankResult getRankedNodes(
			@DefaultValue( "eigenvector-centrality" ) @QueryParam( "method" ) String method,
			@DefaultValue( "10" ) @QueryParam( "limit" ) Long limit )
	{
		Neo4jGraph graph = null;
		try
		{
			graph = new Neo4jGraph( "/Users/jldevezas/Desktop/testdb" );
			IndexManager index = graph.getRawGraph().index();
			Index<Node> nodes = index.forNodes( "vertices" );
			IndexHits<Node> hits = nodes.query(
					"url", new QueryContext( "*" ).sort( "eigenvectorCentrality" ) );

			result = new ArrayList<String>();
			int c = 0;
			for ( Node hit : hits )
			{
				//if ( c++ >= limit ) break;
				String url = hit.getProperty( "url" ).toString();
				Long eigenvectorCentrality;
				try
				{
					eigenvectorCentrality = (Long) hit.getProperty( "eigenvectorCentrality" );
				}
				catch ( Exception e )
				{
					eigenvectorCentrality = new Long( 0 );
				}
				System.out.println( url + "\t" + eigenvectorCentrality );
				result.add( hit.getProperty( "url" ).toString() );
			}
		}
		finally
		{
			if ( graph != null ) graph.shutdown();
		}

		return new NodeRankResult( method, limit );
	}
}
