package pt.up.fe.unite.util;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.commons.lang.ClassUtils;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0406
 * @since 1.6
 */
public class Logging
{
	public static final String DEBUG_PATTERN =
		"[%d{yyyy-MM-dd HH:mm:ss,SSS}] [%p] %c [%M,%L] - %m%n";
	public static final String LONG_PATTERN = "[%d{yyyy-MM-dd HH:mm:ss}] [%p] %c - %m%n";
	public static final String SHORT_PATTERN = "[%p] %c - %m%n";

	private static boolean initialized = false;
	private static String pattern = SHORT_PATTERN;

	static
	{
		setup( pattern );
	}

	public static void setup( String pattern )
	{
		if ( initialized && Logging.pattern.equals( pattern ) ) return;
		Logging.pattern = pattern;

		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure( new ConsoleAppender( new PatternLayout( pattern ) ) );
		Logger.getRootLogger().setLevel( Level.INFO );
	}
	
	public static Logger getLogger( Object object )
	{
		if ( pattern.equals( SHORT_PATTERN ) )
			return Logger.getLogger(
					ClassUtils.getShortCanonicalName( object.getClass().getName() ) );

		return Logger.getLogger( object.getClass().getName() );
	}

	public static Logger getLogger( String name )
	{
		if ( pattern.equals( SHORT_PATTERN ) )
			return Logger.getLogger(
					ClassUtils.getShortCanonicalName( name ) );

		return Logger.getLogger( name );
	}
}
