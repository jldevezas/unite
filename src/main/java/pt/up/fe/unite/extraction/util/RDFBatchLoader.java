package pt.up.fe.unite.extraction.util;

import pt.up.fe.unite.util.Logging;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.Logger;

public class RDFBatchLoader
{
	private Logger log;

	public RDFBatchLoader( String dirPath, String fileFilterRegexp )
	{
		log = Logging.getLogger( this.getClass().getName() );

		try
		{
			FileSystemManager fsManager = VFS.getManager();
			FileObject jarFile = fsManager.resolveFile( dirPath );

			FileObject[] children = jarFile.getChildren();
			System.out.println( "Children of " + jarFile.getName().getURI() );
			for ( int i = 0; i < children.length; i++ )
			{
				System.out.println( children[ i ].getName().getBaseName() );
			}
		}
		catch ( FileSystemException e )
		{
			log.error( e );
		}
	}
}
