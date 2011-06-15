package pt.up.fe.unite.application.cli;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.extraction.reader.Reader;
import pt.up.fe.unite.extraction.writer.Writer;
import pt.up.fe.unite.mining.communities.util.CommunityFile;
import pt.up.fe.unite.mining.labels.Labeler;
import pt.up.fe.unite.mining.stats.CommunityStats;
import pt.up.fe.unite.mining.properties.PropertyBuilder;
import pt.up.fe.unite.mining.properties.DegreeBuilder;
import pt.up.fe.unite.application.service.RESTService;

import java.util.Properties;
import java.io.File;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0414
 * @since 1.6
 */
public class Runner
{
	private Logger log;
	private OptionSet options;
	private OptionParser parser;
	
	public Runner( String[] args )
	{
		log = Logging.getLogger( this );

		parser = new OptionParser()
		{
			{
				accepts( "extract" );
				//	.describedAs( "Extract edges from a data source and generate a graph");
				accepts( "communities" );
				accepts( "labels" );
				accepts( "properties" );
				accepts( "server" );
				accepts( "stats" );

				accepts( "debug" ).withOptionalArg().defaultsTo( "DEBUG" );
				accepts( "help" );

				accepts( Reader.SK_HOSTNAME ).withRequiredArg();
				accepts( Reader.SK_DATABASE ).withRequiredArg().ofType( File.class );
				accepts( Reader.SK_USERNAME ).withRequiredArg();
				accepts( Reader.SK_PASSWORD ).withOptionalArg();
				accepts( Reader.SK_TABLE ).withRequiredArg();
				accepts( Reader.SK_SOURCE ).withRequiredArg();
				accepts( Reader.SK_TARGET ).withRequiredArg();
				accepts( Reader.SK_LIMIT ).withRequiredArg();
				
				accepts( Writer.SK_IMPLEMENTATION ).withRequiredArg();
				accepts( Writer.SK_DATABASE ).withRequiredArg();
				accepts( Writer.SK_RELATIONSHIP ).withRequiredArg();
				accepts( Writer.SK_SOURCE_NAME ).withRequiredArg();
				accepts( Writer.SK_TARGET_NAME ).withRequiredArg();
				accepts( Writer.SK_EDGE_ID_NAME ).withRequiredArg();
				accepts( Writer.SK_EDGE_WEIGHT_NAME ).withRequiredArg();

				accepts( Labeler.SK_COMMUNITY_FILE ).withRequiredArg();
				accepts( Labeler.SK_CONDITION_FIELD ).withRequiredArg();
				accepts( Labeler.SK_MAX ).withRequiredArg().ofType( Integer.class );
				
				accepts( PropertyBuilder.SK_DATABASE ).withRequiredArg();
				accepts( PropertyBuilder.SK_IMPLEMENTATION ).withRequiredArg();
				accepts( PropertyBuilder.SK_PROPERTY ).withRequiredArg();
				accepts( PropertyBuilder.SK_KEY_FIELD ).withRequiredArg();
				accepts( DegreeBuilder.SK_TYPE ).withRequiredArg();

				accepts( CommunityFile.SK_LOAD_DIR ).withRequiredArg();
				accepts( CommunityFile.SK_DATABASE ).withRequiredArg();

				accepts( RESTService.SK_DATABASE ).withRequiredArg();

				accepts( CommunityStats.SK_DATABASE ).withRequiredArg();
				accepts( CommunityStats.SK_COMMUNITY_PROPERTY ).withRequiredArg();
				accepts( CommunityStats.SK_FORMAT ).withRequiredArg();
				accepts( CommunityStats.SK_OUT_FILE ).withRequiredArg();
				accepts( CommunityStats.SK_TYPE ).withRequiredArg();
				accepts( CommunityStats.SK_NODE_MATCH ).withRequiredArg();
			}
		};

		try
		{
			options = parser.parse( args );
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
			System.exit( 1 );
		}
	}

	public void run()
	{
		if ( options.has( "help" ) )
		{
			try
			{
				parser.printHelpOn( System.out );
			}
			catch ( Exception e )
			{
				log.error( "Error printing help", e );
			}

			System.exit( 0 );
		}
		
		if ( options.has( "debug" ) )
			Logger.getRootLogger().setLevel(
					Level.toLevel( (String) options.valueOf( "debug" ) ) );

		if ( options.has( "extract" ) )
			( new GraphBuilderRunner( options ) ).run();
		else if ( options.has( "communities" ) )
			( new CommunityDetectRunner( options ) ).run();
		else if ( options.has( "labels" ) )
			( new LabelerRunner( options ) ).run();
		else if ( options.has( "properties" ) )
			( new PropertyBuilderRunner( options ) ).run();
		else if ( options.has( "server" ) )
			( new ServerRunner( options ) ).run();
		else if ( options.has( "stats" ) )
			( new StatsRunner( options ) ).run();
	}

	public static void loadReaderSettings( OptionSet options, Properties settings )
	{
		if ( options.hasArgument( Reader.SK_USERNAME ) )
			settings.setProperty( Reader.SK_USERNAME,
					options.valueOf( Reader.SK_USERNAME ).toString() );

		if ( options.has( Reader.SK_PASSWORD ) )
		{
			if ( options.hasArgument( Reader.SK_PASSWORD ) )
				settings.setProperty( Reader.SK_PASSWORD,
						options.valueOf( Reader.SK_PASSWORD ).toString() );
			else
				settings.setProperty( Reader.SK_PASSWORD,
						new String( System.console().readPassword( "Enter password: " ) ) );
		}

		if ( options.hasArgument( Reader.SK_HOSTNAME ) )
			settings.setProperty( Reader.SK_HOSTNAME,
					options.valueOf( Reader.SK_HOSTNAME ).toString() );

		if ( options.hasArgument( Reader.SK_DATABASE ) )
			settings.setProperty( Reader.SK_DATABASE,
					options.valueOf( Reader.SK_DATABASE ).toString() );

		if ( options.hasArgument( Reader.SK_TABLE ) )
			settings.setProperty( Reader.SK_TABLE,
					options.valueOf( Reader.SK_TABLE ).toString() );

		if ( options.hasArgument( Reader.SK_SOURCE ) )
			settings.setProperty( Reader.SK_SOURCE,
					options.valueOf( Reader.SK_SOURCE ).toString() );

		if ( options.hasArgument( Reader.SK_TARGET ) )
			settings.setProperty( Reader.SK_TARGET,
					options.valueOf( Reader.SK_TARGET ).toString() );

		if ( options.hasArgument( Reader.SK_LIMIT ) )
			settings.setProperty( Reader.SK_LIMIT,
					options.valueOf( Reader.SK_LIMIT ).toString() );
	}

	public static void loadWriterSettings( OptionSet options, Properties settings )
	{
		if ( options.hasArgument( Writer.SK_IMPLEMENTATION ) )
			settings.setProperty( Writer.SK_IMPLEMENTATION,
					options.valueOf( Writer.SK_IMPLEMENTATION ).toString() );

		if ( options.hasArgument( Writer.SK_DATABASE ) )
			settings.setProperty( Writer.SK_DATABASE,
					options.valueOf( Writer.SK_DATABASE ).toString() );

		if ( options.hasArgument( Writer.SK_RELATIONSHIP ) )
			settings.setProperty( Writer.SK_RELATIONSHIP,
					options.valueOf( Writer.SK_RELATIONSHIP ).toString() );

		if ( options.hasArgument( Writer.SK_SOURCE_NAME ) )
			settings.setProperty( Writer.SK_SOURCE_NAME,
					options.valueOf( Writer.SK_SOURCE_NAME ).toString() );

		if ( options.hasArgument( Writer.SK_TARGET_NAME ) )
			settings.setProperty( Writer.SK_TARGET_NAME,
					options.valueOf( Writer.SK_TARGET_NAME ).toString() );

		if ( options.hasArgument( Writer.SK_EDGE_ID_NAME ) )
			settings.setProperty( Writer.SK_EDGE_ID_NAME,
					options.valueOf( Writer.SK_EDGE_ID_NAME ).toString() );

		if ( options.hasArgument( Writer.SK_EDGE_WEIGHT_NAME ) )
			settings.setProperty( Writer.SK_EDGE_WEIGHT_NAME,
					options.valueOf( Writer.SK_EDGE_WEIGHT_NAME ).toString() );
	}
	
	public static void loadPropertyBuilderSettings( OptionSet options, Properties settings )
	{
		if ( options.hasArgument( PropertyBuilder.SK_DATABASE ) )
			settings.setProperty( PropertyBuilder.SK_DATABASE,
					options.valueOf( PropertyBuilder.SK_DATABASE ).toString() );
		
		if ( options.hasArgument( PropertyBuilder.SK_IMPLEMENTATION ) )
			settings.setProperty( PropertyBuilder.SK_IMPLEMENTATION,
					options.valueOf( PropertyBuilder.SK_IMPLEMENTATION ).toString() );

		if ( options.hasArgument( DegreeBuilder.SK_TYPE ) )
			settings.setProperty( DegreeBuilder.SK_TYPE,
					options.valueOf( DegreeBuilder.SK_TYPE ).toString() );
	
		// TODO Centrality needs a type too
	}
}
