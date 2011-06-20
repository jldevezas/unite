package com.josedevezas.unite.extraction.util;

import com.josedevezas.unite.util.Logging;
import com.josedevezas.unite.util.Trackable;
import com.josedevezas.unite.util.ProgressTracker;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.pgm.impls.sail.impls.NativeStoreSailGraph;

public class RDFBatchLoader implements Trackable
{
	private Logger log;
	private FileObject[] files;
	private String graphDbPath;
	private String baseURI;
	private long filesLoaded;

	public RDFBatchLoader( String vfsPath, String graphDbPath, String baseURI )
	{
		this( vfsPath, graphDbPath, baseURI, ".*" );
	}

	public RDFBatchLoader( String vfsPath, String graphDbPath,
			String baseURI, final String fileFilter )
	{
		log = Logging.getLogger( this.getClass().getName() );
		filesLoaded = 0;
		this.graphDbPath = graphDbPath;
		this.baseURI = baseURI;

		FileObject vfsFile = null;

		try
		{
			log.info( "Opening " + vfsPath + "." );
			vfsFile = VFS.getManager().resolveFile( vfsPath );

			log.info( "Finding files that match the provided filter." );
			files = vfsFile.findFiles( new FileSelector() {
				public boolean includeFile( FileSelectInfo fileInfo ) throws FileSystemException {
					if ( fileInfo.getFile().getType() == FileType.FILE
						&& fileInfo.getFile().getName().getBaseName().matches( fileFilter ) )
					{
						return true;
					}

					return false;
				}

				public boolean traverseDescendents( FileSelectInfo fileInfo ) {
					return true;
				}
			});
		}
		catch ( FileSystemException e )
		{
			log.error( e );
		}
		finally
		{
			try
			{
				if ( vfsFile != null )
				{
					log.info( "Closing " + vfsPath + "." );
					vfsFile.close();
				}
			}
			catch ( FileSystemException e )
			{
				log.error( e );
			}
		}
	}

	public void loadFiles()
	{
		ProgressTracker tracker = new ProgressTracker( 30, TimeUnit.SECONDS, this );
		NativeStoreSailGraph g = null;

		try
		{
			g = new NativeStoreSailGraph( graphDbPath );

			for ( int i = 0; i < files.length; i++ )
			{
				InputStream inputStream = files[ i ].getContent().getInputStream();
				g.loadRDF( inputStream, baseURI, "rdf-xml", null );
				inputStream.close();
				filesLoaded++;
			}

			tracker.stop();
		}
		catch ( Exception e )
		{
			log.error( e );
		}
		finally
		{
			if ( g != null ) g.shutdown();
		}
	}

	public void track()
	{
		log.info( filesLoaded + " out of " + files.length + " files loaded so far." );
	}
}
