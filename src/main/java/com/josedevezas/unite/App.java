package com.josedevezas.unite;

import com.josedevezas.unite.util.Logging;
import com.josedevezas.unite.application.cli.Runner;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.ConsoleAppender;

import joptsimple.OptionSet;

/**
 * This is the main class, that will call the CLI.
 *
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class App 
{
	private static final Logger log;
	
	static
	{
		BasicConfigurator.configure( new ConsoleAppender(
					new PatternLayout( "[%d{yyyy-MM-dd HH:mm:ss}] [%p] %c [%M,%L] %m%n" ) ) );

		Logger.getRootLogger().setLevel( Level.INFO );
		log = Logging.getLogger( App.class.getName() );
	}

	public static void main( String[] args )
	{
		new Runner( args ).run();
	}
}
