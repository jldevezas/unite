package pt.up.fe.unite.application.cli;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.extraction.reader.Reader;
import pt.up.fe.unite.mining.properties.PropertyBuilder;
import pt.up.fe.unite.mining.properties.DegreeBuilder;
import pt.up.fe.unite.mining.properties.CentralityBuilder;

import java.util.Properties;

import org.apache.log4j.Logger;

import joptsimple.OptionSet;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0414
 * @since 1.6
 */
public class PropertyBuilderRunner 
{
	private Logger log;
	private Properties settings;
	private String property;

	public PropertyBuilderRunner( OptionSet options )
	{
		log = Logging.getLogger( this );
		settings = new Properties( PropertyBuilder.DEFAULT_SETTINGS );
		property = null;

		Runner.loadPropertyBuilderSettings( options, settings );
		
		if ( options.hasArgument( PropertyBuilder.SK_PROPERTY ) )
			property = options.valueOf( PropertyBuilder.SK_PROPERTY ).toString();
	}

	public void run()
	{
		PropertyBuilder builder = null;
		try
		{
			if ( property == null )
				throw new IllegalArgumentException( "-" + PropertyBuilder.SK_PROPERTY +
						" must be set to either 'degree' or 'centrality'" );

			if ( property.equals( PropertyBuilder.SV_PROPERTY_DEGREE ) )
			{
				Properties degreeSettings = new Properties( DegreeBuilder.DEFAULT_SETTINGS );
				degreeSettings.putAll( settings );
				builder = new DegreeBuilder( degreeSettings );
			}
			else if ( property.equals( PropertyBuilder.SV_PROPERTY_CENTRALITY ) )
				builder = new CentralityBuilder( settings );
			else
				throw new IllegalArgumentException( "-" + PropertyBuilder.SK_PROPERTY +
						" must be set to either 'degree', 'centrality' or 'labels'" );

			builder.run();
		}
		catch ( IllegalArgumentException e )
		{
			log.error( e.getMessage() );
		}
		finally
		{
			if ( builder != null ) builder.close();
		}
	}
}
