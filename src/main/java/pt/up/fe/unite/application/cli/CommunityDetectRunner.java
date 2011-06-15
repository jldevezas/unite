package pt.up.fe.unite.application.cli;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.mining.communities.util.CommunityFile;

import java.io.File;
import java.util.LinkedList;

import joptsimple.OptionSet;

import org.apache.log4j.Logger;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0415
 * @since 1.6
 */
public class CommunityDetectRunner 
{
	private Logger log;
	private String loadDir;
	private String database;

	public CommunityDetectRunner( OptionSet options )
	{
		log = Logging.getLogger( this );

		if ( ! options.hasArgument( CommunityFile.SK_LOAD_DIR ) )
			throw new IllegalArgumentException( "--" + CommunityFile.SK_LOAD_DIR +
					" must be set to a directory containing the '.community' files." );

		if ( ! options.hasArgument( CommunityFile.SK_DATABASE ) )
			throw new IllegalArgumentException( "--" + CommunityFile.SK_DATABASE +
					" must be set to the graph DB path." );

		this.loadDir = options.valueOf( CommunityFile.SK_LOAD_DIR ).toString();
		this.database = options.valueOf( CommunityFile.SK_DATABASE ).toString();
	}

	public void run()
	{
		File directory = new File( loadDir );
		if ( ! directory.isDirectory() )
		{
			log.error( directory + " must be a directory" );
			return;
		}

		String[] files = directory.list();
		LinkedList<String> communityFiles = new LinkedList<String>();

		for ( String file : files )
			if ( file.endsWith( ".community" ) )
				communityFiles.add( directory.getAbsolutePath() + "/" + file );

		CommunityFile.readInto( database, communityFiles, "url" );
	}
}
