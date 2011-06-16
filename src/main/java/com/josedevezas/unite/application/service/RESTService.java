package com.josedevezas.unite.application.service;

import com.josedevezas.unite.util.Logging;

import java.util.Map;
import java.util.HashMap;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.util.LoggerUtils;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.json.JSONConfiguration;

import org.apache.log4j.Logger;

import org.apache.commons.lang.ClassUtils;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0413
 * @since 1.6
 */
public class RESTService 
{
	public static final String SK_DATABASE = "s-database";

	private Logger log;
	private String database;

	public RESTService( String database )
	{
		log = Logging.getLogger( this );
		this.database = database;

		if ( database == null )
			throw new IllegalArgumentException( "--" + RESTService.SK_DATABASE +
					" must be set to the graph database path" );
	}

	public void run()
	{
		final String baseUri = "http://localhost:9000/";
		final Map<String,String> initParams = new HashMap<String,String>();

		initParams.put(
				"com.sun.jersey.config.property.packages",
				"com.josedevezas.unite.application.service" );

		ClientConfig clientConfig = new DefaultClientConfig();
		clientConfig.getFeatures().put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );
		Client client = Client.create( clientConfig );

		SelectorThread threadSelector = null;
		try
		{
			log.info( "Starting grizzly..." );
			threadSelector = GrizzlyWebContainerFactory.create( baseUri, initParams );

			log.info( "Unite server running at " + baseUri );
			log.info( "Hit enter to stop it..." );
			System.in.read();
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
		finally
		{
			if ( threadSelector != null ) threadSelector.stopEndpoint();
			System.exit( 0 );
		}
	}

	public void close()
	{
	}
}
