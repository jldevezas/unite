package pt.up.fe.unite.application.cli;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.mining.stats.CommunityStats;

import java.io.OutputStream;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;

import joptsimple.OptionSet;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0524
 * @since 1.6
 */
public class StatsRunner
{
	private Logger log;
	private String database;
	private String communityProperty;
	private String format;
	private String outFile;
	private String type;
	private String nodeMatch;

	public StatsRunner( OptionSet options )
	{
		log = Logging.getLogger( this );
		database = null;
		communityProperty = null;
		format = "plain";
		outFile = null;
		type = null;
		nodeMatch = ".*";

		if ( options.hasArgument( CommunityStats.SK_DATABASE ) )
			database = options.valueOf( CommunityStats.SK_DATABASE ).toString();

		if ( options.hasArgument( CommunityStats.SK_COMMUNITY_PROPERTY ) )
			communityProperty = options.valueOf( CommunityStats.SK_COMMUNITY_PROPERTY ).toString();

		if ( options.hasArgument( CommunityStats.SK_FORMAT ) )
			format = options.valueOf( CommunityStats.SK_FORMAT ).toString();

		if ( options.hasArgument( CommunityStats.SK_OUT_FILE ) )
			outFile = options.valueOf( CommunityStats.SK_OUT_FILE ).toString();

		if ( options.hasArgument( CommunityStats.SK_TYPE ) )
			type = options.valueOf( CommunityStats.SK_TYPE ).toString();
		
		if ( options.hasArgument( CommunityStats.SK_NODE_MATCH ) )
			nodeMatch = options.valueOf( CommunityStats.SK_NODE_MATCH ).toString();
	}

	public void run()
	{
		try
		{
			OutputStream out = System.out;
			if ( outFile != null ) out = new FileOutputStream( outFile );
				
			CommunityStats stats = new CommunityStats(
					database, communityProperty, nodeMatch, format, out );

			if ( type == null || type.equals( "nodesPerCommunity" ) )
				stats.nodesPerCommunity();
			else if ( type.equals( "nodeOverlap" ) )
				stats.nodeOverlap();
		}
		catch ( Exception e )
		{
			log.error( e );
		}
	}
}
