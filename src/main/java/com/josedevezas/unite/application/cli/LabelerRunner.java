package com.josedevezas.unite.application.cli;

import com.josedevezas.unite.util.Logging;
import com.josedevezas.unite.extraction.reader.Reader;
import com.josedevezas.unite.extraction.reader.MySQLReader;
import com.josedevezas.unite.mining.labels.Labeler;
import com.josedevezas.unite.mining.labels.HtmlTfIdfLabeler;
import com.josedevezas.unite.mining.properties.PropertyBuilder;
import com.josedevezas.unite.mining.properties.CustomPropertyBuilder;

import java.util.List;
import java.util.Properties;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;

import joptsimple.OptionSet;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0414
 * @since 1.6
 */
public class LabelerRunner 
{
	private Logger log;
	private Properties settings;
	private String communityFile;
	private String conditionField;
	private String keyField;
	private int maxLabels;

	public LabelerRunner( OptionSet options )
	{
		log = Logging.getLogger( this );
		settings = new Properties( Reader.DEFAULT_SETTINGS );
		communityFile = null;
		conditionField = null;
		keyField = null;
		maxLabels = 3;

		settings.putAll( PropertyBuilder.DEFAULT_SETTINGS );
		Runner.loadReaderSettings( options, settings );
		
		if ( options.hasArgument( Labeler.SK_COMMUNITY_FILE ) )
			communityFile = options.valueOf( Labeler.SK_COMMUNITY_FILE ).toString();
		
		if ( options.hasArgument( Labeler.SK_CONDITION_FIELD ) )
			conditionField = options.valueOf( Labeler.SK_CONDITION_FIELD ).toString();

		if ( options.hasArgument( PropertyBuilder.SK_KEY_FIELD ) )
			keyField = options.valueOf( PropertyBuilder.SK_KEY_FIELD ).toString();

		if ( options.hasArgument( Labeler.SK_MAX ) )
			maxLabels = Integer.parseInt( options.valueOf( Labeler.SK_MAX ).toString() );

		Runner.loadPropertyBuilderSettings( options, settings );
	}

	public void run()
	{
		List<String> nodes = null;
		try
		{
			if ( communityFile == null )
				throw new IllegalArgumentException( "-" + Labeler.SK_COMMUNITY_FILE +
						" must be set to a community listing file" );

			if ( conditionField == null )
				throw new IllegalArgumentException( "-" + Labeler.SK_CONDITION_FIELD +
						" must match the database field of the elements listed in the community file" );

			if ( keyField == null )
				throw new IllegalArgumentException( "-" + PropertyBuilder.SK_KEY_FIELD +
						" must be set to a node key property identifier" );

			nodes = FileUtils.readLines( new File( communityFile ) );

			StringBuilder whereCondition = new StringBuilder();
			boolean first = true;
			for ( String node : nodes )
			{
				if ( first )
					first = false;
				else
					whereCondition.append( " OR " );
				whereCondition.append( conditionField + " = '" + node + "'" );
			}
			log.debug( "Using condition '" + whereCondition + "'" );

			settings.setProperty( Reader.SK_CONDITION, whereCondition.toString() );

			// TODO Make the reader and the labeler optional.
			Reader reader = new MySQLReader( settings );
			Labeler labeler = new HtmlTfIdfLabeler( reader );
			labeler.run();

			String communityDescription = nodes.toString();
			if ( communityDescription.length() > 200 )
				communityDescription = communityDescription.substring( 0, 200 ) + " ...";

			List<String> labels = labeler.getLabels( maxLabels );

			log.debug( communityDescription + " labeled as '" + labels + "'" );

			// TODO make the property names optional.
			PropertyBuilder propertyBuilder =
				new CustomPropertyBuilder( settings, nodes, "url", labels, "labels", true );
			propertyBuilder.run();
			propertyBuilder.close();
		}
		catch ( IOException e )
		{
			log.error( e.getMessage() );
			System.exit( 1 );
		}
		catch ( Exception e )
		{
			log.error( e );
			System.exit( 1 );
		}
	}
}
