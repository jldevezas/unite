package com.josedevezas.unite.extraction.util;

import com.josedevezas.unite.util.Pair;
import com.josedevezas.unite.util.Logging;
import com.josedevezas.unite.extraction.parser.Parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class PairProcessor extends Thread
{
	private Logger log;
	private LinkedBlockingQueue<Pair<String,String>> result;
	private Parser leftParser;
	private Parser rightParser;

	public PairProcessor(
			Pair<String,String> content,
			Pair<Class,Class> parser,
			LinkedBlockingQueue<Pair<String,String>> result )
	{
		this.result = result;
		log = Logging.getLogger( this );
		
		try
		{
			Constructor leftParserConstructor =
				parser.getLeft().getConstructor( String.class );
			this.leftParser = ( Parser )leftParserConstructor.newInstance(
					new Object[] { content.getLeft() } );

			Constructor rightParserConstructor =
				parser.getRight().getConstructor( String.class );
			this.rightParser = ( Parser )rightParserConstructor.newInstance(
					new Object[] { content.getRight() } );
		}
		catch ( Exception e )
		{
			log.error( "Error constructing parsers", e );
		}
	}

	public void run()
	{
		try
		{
			for ( String source : leftParser.parse() )
			{
				for ( String target : rightParser.parse() )
				{
					log.trace( "Putting result in queue(" + result.size() + ")" );
					result.put( new Pair<String,String>( source, target ) );
				}
			}
		}
		catch ( InterruptedException e )
		{
			log.error( e.getMessage() );
		}
	}
}
