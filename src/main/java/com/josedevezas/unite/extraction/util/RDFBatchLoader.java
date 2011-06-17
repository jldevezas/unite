package com.josedevezas.unite.extraction.util;

import com.josedevezas.unite.util.Logging;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.pgm.impls.sail.impls.NativeStoreSailGraph;

public class RDFBatchLoader
{
	private Logger log;
	private FileObject[] files;
	private String graphDbPath;
	private String baseURI;

	public RDFBatchLoader( String vfsPath, String graphDbPath, String baseURI )
	{
		this( vfsPath, graphDbPath, baseURI, ".*" );
	}

	public RDFBatchLoader( String vfsPath, String graphDbPath,
			String baseURI, final String fileFilter )
	{
		log = Logging.getLogger( this.getClass().getName() );
		this.graphDbPath = graphDbPath;
		this.baseURI = baseURI;

		try
		{
			FileSystemManager fsManager = VFS.getManager();
			FileObject vfsFile = fsManager.resolveFile( vfsPath );

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
	}

	public void loadFiles()
	{
		NativeStoreSailGraph g = null;

		try
		{
			g = new NativeStoreSailGraph( graphDbPath );

			for ( int i = 0; i < files.length; i++ )
			{
				InputStream inputStream = files[ i ].getContent().getInputStream();
				g.loadRDF( inputStream, baseURI, "rdf-xml", null );
				inputStream.close();
			}
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
}
