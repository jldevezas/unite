package pt.up.fe.unite.application.cli;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.application.service.RESTService;

import org.apache.log4j.Logger;

import joptsimple.OptionSet;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0415
 * @since 1.6
 */
public class ServerRunner 
{
	private Logger log;
	private String database;

	public ServerRunner( OptionSet options )
	{
		log = Logging.getLogger( this );
		database = null;

		if ( options.hasArgument( RESTService.SK_DATABASE ) )
			database = options.valueOf( RESTService.SK_DATABASE ).toString();
	}

	public void run()
	{
		RESTService service = null;

		try
		{
			service = new RESTService( database );
			service.run();
		}
		catch ( IllegalArgumentException e )
		{
			log.error( e.getMessage() );
		}
		finally
		{
			if ( service != null ) service.close();
		}
	}
}
