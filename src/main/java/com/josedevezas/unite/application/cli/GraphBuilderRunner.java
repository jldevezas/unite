package com.josedevezas.unite.application.cli;

import com.josedevezas.unite.util.Pair;
import com.josedevezas.unite.util.Logging;
import com.josedevezas.unite.extraction.GraphBuilder;
import com.josedevezas.unite.extraction.reader.Reader;
import com.josedevezas.unite.extraction.reader.MySQLReader;
import com.josedevezas.unite.extraction.parser.UrlDomainParser;
import com.josedevezas.unite.extraction.parser.AnchorDomainParser;
import com.josedevezas.unite.extraction.writer.Writer;
import com.josedevezas.unite.extraction.writer.BlueprintsWriter;

import java.util.Properties;

import org.apache.log4j.Logger;

import joptsimple.OptionSet;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0405
 * @since 1.6
 */
public class GraphBuilderRunner 
{
	private Properties readerSettings;
	private Properties writerSettings;
	private Logger log;
	private GraphBuilder builder;

	public GraphBuilderRunner( OptionSet options )
	{
		log = Logging.getLogger( this );
		readerSettings = new Properties( Reader.DEFAULT_SETTINGS );
		writerSettings = new Properties( Writer.DEFAULT_SETTINGS );

		Runner.loadReaderSettings( options, readerSettings );
		Runner.loadWriterSettings( options, writerSettings );

		Reader reader = null;
		Writer writer = null;

		try
		{
			reader = new MySQLReader( readerSettings );
			Pair<Class,Class> parser =
				new Pair<Class,Class>( UrlDomainParser.class, AnchorDomainParser.class );

			// TODO Make it optional to choose the Writer.
			//Writer writer = new TextWriter( "/Users/jldevezas/Desktop/graph.txt" );
			//Writer writer = new Neo4jWriter( wSettings );
			writer = new BlueprintsWriter( writerSettings );

			builder = new GraphBuilder( reader, parser, writer );
		}
		catch ( IllegalArgumentException e )
		{
			log.error( "Wrong settings, check the API", e );
		}
	}

	public void run()
	{
		try
		{
			builder.run();
		}
		catch ( IllegalArgumentException e )
		{
			log.error( "Wrong settings, check the API", e );
		}
	}
}
