package com.josedevezas.unite.extraction.writer;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class TextWriter extends Writer
{
	private BufferedWriter file;

	/**
	 * Creates a TextWriter that writes to the console.
	 *
	 * The character encoding is always utf-8.
	 */
	public TextWriter()
	{
		super();

		log.info( "Writing to console" );

		try
		{
			this.file = new BufferedWriter( new OutputStreamWriter(
						System.out, "UTF-8" ) );
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
	}

	/**
	 * Creates a TextWriter that writes to a file.
	 *
	 * The character encoding is always utf-8.
	 */
	public TextWriter( String filename )
	{
		super();

		log.info( "Writing to " + filename );
		
		try
		{
			this.file = new BufferedWriter( new OutputStreamWriter(
						new FileOutputStream( filename ), "UTF-8" ) );
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
	}

	public void write( String source, String target )
	{
		try
		{
			log.trace( "Writing " + source + " -> " + target );
			file.write( source + "\t" + target );
			file.newLine();
		}
		catch ( IOException e )
		{
			log.error( e.getMessage() );
		}
	}

	public void close()
	{
		log.info( "Closing Writer" );
		try
		{
			file.flush();
			file.close();
		}
		catch ( Exception e )
		{
			log.warn( e.getMessage() );
		}
	}
}
