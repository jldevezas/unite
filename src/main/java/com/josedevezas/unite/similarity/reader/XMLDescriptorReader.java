package com.josedevezas.unite.similarity.reader;

import com.josedevezas.unite.util.Trackable;
import com.josedevezas.unite.util.ProgressTracker;
import com.josedevezas.unite.similarity.helper.XMLHandler;
import com.josedevezas.unite.similarity.helper.MarsupilamiIndexHandler;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import java.util.concurrent.TimeUnit;
import java.net.InetSocketAddress;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import net.spy.memcached.MemcachedClient;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0419
 * @since 1.6
 */
public class XMLDescriptorReader extends DescriptorReader implements Trackable
{
	private String filename;
	private XMLReader reader;
	private XMLHandler handler;
	private InputStream inputStream;
	private Deque<String> pendingKeys;
	private MemcachedClient cache;
	private Map<String,List<Double>> internalCache;

	public XMLDescriptorReader( String filename, XMLHandler handler )
		throws FileNotFoundException, IOException, SAXException
	{
		super();

		this.filename = filename;
		this.handler = handler;
		this.pendingKeys = new ArrayDeque<String>( handler.getKeys() );

		this.inputStream = new FileInputStream( filename );
		if ( filename.endsWith( ".gz" ) )
			this.inputStream = new GZIPInputStream( inputStream );

		this.reader = XMLReaderFactory.createXMLReader();

		if ( handler.isUsingInternalCache() )
		{
			this.internalCache = handler.getInternalCache();
		}
		else
		{
			String server = handler.getServer();
			int port = handler.getPort();
			this.cache = new MemcachedClient( new InetSocketAddress( server, port ) );
		}
	}

	public void prepareCache()
	{
		try
		{
			ProgressTracker progressTracker = new ProgressTracker( 30, TimeUnit.SECONDS, this );

			log.info( "Parsing " + filename );

			reader.setContentHandler( handler );
			reader.setErrorHandler( handler );
			reader.parse( new InputSource( inputStream ) );

			pendingKeys = new ArrayDeque<String>( handler.getKeys() );

			progressTracker.stop();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			log.error( e );
			System.exit( 1 );
		}
	}

	public List<Double> next()
	{
		Object value = null;

		try
		{
			id = pendingKeys.pop();
			if ( handler.isUsingInternalCache() )
				value = internalCache.get( id );
			else
				value = cache.get( id );
		}
		catch ( NoSuchElementException e )
		{
			log.debug( "No more elements to process" );
		}

		return (List<Double>) value;
	}

	public void close()
	{
		try
		{
			if ( handler != null ) handler.close();
		}
		catch ( Exception e )
		{
			log.error( e );
		}
	}

	public XMLDescriptorReader clone()
	{
		try
		{
			return new XMLDescriptorReader( filename, handler );
		}
		catch ( Exception e )
		{
			return null;
		}
	}

	public void track()
	{
		log.info( handler.getItemsCached() + " items cached so far" );
	}
}
