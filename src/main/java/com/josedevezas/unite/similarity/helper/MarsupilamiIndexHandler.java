package com.josedevezas.unite.similarity.helper;

import com.josedevezas.unite.util.Logging;
import com.josedevezas.unite.similarity.helper.XMLHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class MarsupilamiIndexHandler extends XMLHandler
{
	private Logger log;

	private MemcachedClient cache;
	private XMLReader reader;
	private String thisElement;
	private String thisId;
	private Deque<String> keys;
	private String server;
	private int port;
	private long itemsCached;
	private boolean useInternalCache;
	private Map<String,List<Double>> internalCache;

	public MarsupilamiIndexHandler( String server, int port )
		throws IOException
	{
		this( server, port, false, false );
	}

	public MarsupilamiIndexHandler(
			String server, int port, boolean useInternalCache, boolean flushCache )
		throws IOException
	{
		super();

		System.setProperty(
				"net.spy.log.LoggerImpl",
				"net.spy.memcached.compat.log.Log4JLogger" );
		Logger.getLogger( "net.spy.memcached" ).setLevel( Level.WARN );

		this.log = Logging.getLogger( this );
		this.thisElement = "";
		this.keys = new ArrayDeque<String>();
		this.server = server;
		this.port = port;
		this.itemsCached = 0;
		this.useInternalCache = useInternalCache;

		if ( useInternalCache )
		{
			internalCache = new HashMap<String,List<Double>>();
		}
		else
		{
			log.info( "Writing to cache server on " + server + ":" + port );
			cache = new MemcachedClient( new InetSocketAddress( server, port ) );

			if ( flushCache )
			{
				log.info( "Flushing cache" );
				cache.flush();
			}
		}
	}

	public String getServer()
	{
		return this.server;
	}

	public int getPort()
	{
		return this.port;
	}

	public long getItemsCached()
	{
		return this.itemsCached;
	}

	public boolean isUsingInternalCache()
	{
		return this.useInternalCache;
	}

	public Map<String,List<Double>> getInternalCache()
	{
		return this.internalCache;
	}

	public Deque<String> getKeys()
	{
		return this.keys;
	}

	public void endDocument()
	{
		if ( cache != null )
		{
			log.info( "Closing connection to the cache server" );
			cache.shutdown( 30, TimeUnit.SECONDS );
		}
	}

	public void startElement( String uri, String name, String qName, Attributes atts )
	{
		if ( "".equals( uri ) )
			thisElement = qName;
		else
			thisElement = name;
	}

	public void endElement( String uri, String name, String qName )
	{
		thisElement = "";
	}

	public void characters( char ch[], int start, int length )
	{
		String content = new String( ch, start, length );

		if ( thisElement.equals( "Filename" ) )
			thisId = content;
		else if ( thisElement.equals( "JCD" ) )
		{
			String[] stringDescriptor = content.replace( ',', '.' ).split( ";" );
			Double[] descriptor = new Double[ stringDescriptor.length ];

			int i = 0;
			for ( String stringValue : stringDescriptor )
				descriptor[ i++ ] = Double.valueOf( stringValue );
			
			if ( useInternalCache )
			{
				internalCache.put( thisId, Arrays.asList( descriptor ) );
			}
			else
			{
				// List is Serializable, an array isn't.
				cache.set( thisId, 0, Arrays.asList( descriptor ) );
			}

			keys.push( thisId );
			itemsCached++;
		}	
	}
}
