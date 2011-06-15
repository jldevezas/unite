package pt.up.fe.unite.extraction.reader;

import pt.up.fe.unite.util.Pair;

import java.lang.Throwable;

import java.util.Properties;
import java.util.LinkedList;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class MySQLReader extends Reader
{
	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	private Properties settings;
	private boolean internalError;
	private boolean allFields;

	public MySQLReader( Properties settings )
	{
		super();

		this.settings = settings;
		this.internalError = false;
		this.allFields = false;

		/*
		 * TODO Check if all necessary arguments are set.
		 */

		String whereCondition = "";
		if ( settings.getProperty( SK_CONDITION ) != null )
			whereCondition = " WHERE " + settings.getProperty( SK_CONDITION );

		if ( settings.getProperty( SK_SOURCE ) == null )
			throw new IllegalArgumentException( "Value of SK_SOURCE can't be null" );

		StringBuilder fields = new StringBuilder( settings.getProperty( SK_SOURCE ) );
		if ( settings.getProperty( SK_TARGET ) == null )
		{
			log.info( "Querying for SK_SOURCE only" );
		}
		else
		{
			fields.append( ", " + settings.getProperty( SK_TARGET ) );
			allFields = true;
			log.info( "Querying for SK_SOURCE and SK_TARGET" );
		}
	
		// TODO make the encodings an option	
		String jdbcString = "jdbc:mysql://"
			+ settings.getProperty( SK_HOSTNAME ) + "/"
			+ settings.getProperty( SK_DATABASE )
			+ "?useUnicode=true&characterEncoding=utf8"
			+ "&characterSetResults=utf8&tcpKeepAlive=true"
			+ "&autoReconnect=true";
		
		log.debug( "Using JDBC connection string '" + jdbcString + "'" );
		
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" ).newInstance();

			log.info( "Connecting to " + settings.getProperty( SK_HOSTNAME ) );
			connection = DriverManager.getConnection(
					jdbcString,
					settings.getProperty( SK_USERNAME ),
					settings.getProperty( SK_PASSWORD ) );
			connection.setReadOnly( true );
			connection.setAutoCommit( false );
			
			// Streamed MySQL fetching, since it has no support for cursors.
			statement = connection.createStatement(
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY );
			statement.setFetchSize( Integer.MIN_VALUE );
			
			String maxRows = settings.getProperty( SK_LIMIT );
			if ( maxRows != null )
			{
				statement.setMaxRows( Integer.parseInt( maxRows ) );
				log.info( "Maximum rows set to " + statement.getMaxRows() );
			}

			log.info( "Querying table " + settings.getProperty( SK_TABLE ) );
			resultSet = statement.executeQuery(
					"SELECT SQL_NO_CACHE " + settings.getProperty( SK_SOURCE )
					+ ", " + settings.getProperty( SK_TARGET ) + " FROM "
					+ settings.getProperty( SK_TABLE ) + whereCondition );
		}
		catch ( SQLException e )
		{
			// TODO better manage exceptions, by interrupting program flow.
			log.error( "[MySQL Error " + e.getSQLState() + "] " + e.getMessage() );
			internalError = true;
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
	}

	public Pair<String,String> next()
	{
		if ( internalError )
		{
			log.trace( "Error occurred, silently finishing" );
			return Pair.NULL;
		}

		try
		{
			log.trace( "Checking for more items" );
			if ( resultSet.next() )
			{
				log.trace( "Next item exists" );
				String source = resultSet.getString( settings.getProperty( SK_SOURCE ) );

				String target = null;
				if ( allFields ) resultSet.getString( settings.getProperty( SK_TARGET ) );

				if ( source == null )
				{
					log.warn( settings.getProperty( SK_SOURCE ) + " is null, skipping" );
					return null;
				}

				if ( target == null && allFields )
				{
					log.warn( settings.getProperty( SK_TARGET ) + " is null, skipping" );
					return null;
				}

				log.trace( "Returning new content Pair" );
				return new Pair<String,String>( source, target );
			}
		}
		catch ( SQLException e )
		{
			log.warn( e.getMessage() );
		}

		log.trace( "Returning NULL Pair" );
		return Pair.NULL;
	}

	public void close()
	{
		log.info( "Disconnecting from " + settings.getProperty( SK_HOSTNAME ) );

		try
		{
			resultSet.close();
			statement.close();
			connection.close();
		}
		catch ( Exception e )
		{
			log.warn( "Couldn't close MySQLReader correctly" );
		}
	}
}
