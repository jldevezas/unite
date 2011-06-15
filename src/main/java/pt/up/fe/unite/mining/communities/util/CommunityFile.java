package pt.up.fe.unite.mining.communities.util;

import pt.up.fe.unite.util.Logging;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.io.File;

import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * Class to handle community files.
 *
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0414
 * @since 1.6
 */
public class CommunityFile
{
	public static final String SK_LOAD_DIR = "c-load-dir";
	public static final String SK_DATABASE = "c-database";

	private static final String COMMUNITIES_PROPERTY = "communities";

	private static final Logger log = Logging.getLogger( CommunityFile.class.getName() );

	public static void readInto(
			String graphDbPath,
			List<String> communityFiles,
			String keyProperty )
	{
		Neo4jGraph graph = null;
		Index<Vertex> index = null;
		
		try
		{
			log.info( "Loading community IDs into " + graphDbPath );
			graph = new Neo4jGraph( graphDbPath );
			index = ( (IndexableGraph) graph ).getIndex( Index.VERTICES, Vertex.class );

			for ( String communityFile : communityFiles )
			{
				log.info( "Loading community file " + communityFile );
				String communityId = FilenameUtils.getBaseName( communityFile );
				List<String> nodeKeys = FileUtils.readLines( new File( communityFile ) );

				log.info( "Writing community ID " + communityId + " to the community nodes" );
				for ( String nodeKey : nodeKeys )
				{
					try
					{
						Vertex vertex = index.get( keyProperty, nodeKey ).iterator().next();

						String[] communitiesProperty =
							(String[]) vertex.getProperty( COMMUNITIES_PROPERTY );

						if ( communitiesProperty == null ) communitiesProperty = new String[0];

						Set<String> communities =
							new HashSet<String>( Arrays.asList( communitiesProperty ) );
						communities.add( communityId );

						vertex.setProperty( COMMUNITIES_PROPERTY, communities.toArray( new String[0] ) );
					}
					catch ( NoSuchElementException e )
					{
						log.warn( "Couldn't find node with " + keyProperty +
								" = " + nodeKey + ", skipping" );
					}
				}
			}
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
		finally
		{
			if ( graph != null )
			{
				log.info( "Closing " + graphDbPath );
				graph.shutdown();
			}
		}
	}
}
