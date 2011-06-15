package pt.up.fe.unite.extraction.reader;

import pt.up.fe.unite.util.Pair;
import pt.up.fe.unite.util.Logging;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class Reader provides a basic framework to implement a content reader.
 *
 * Subclasses should implement their own next method, making their own
 * constructors as needed, for instance to receive a Properties with
 * configuration settings for the Reader, and optionally implementing
 * a close method if necessary.
 *
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public abstract class Reader
{
	public static final String SK_HOSTNAME	= "r-hostname";
	public static final String SK_DATABASE	= "r-database";
	public static final String SK_USERNAME	= "r-username";
	public static final String SK_PASSWORD	= "r-password";
	public static final String SK_TABLE			= "r-table";
	public static final String SK_SOURCE		= "r-source";
	public static final String SK_TARGET		= "r-target";
	public static final String SK_LIMIT			= "r-limit";
	public static final String SK_CONDITION	= "r-condition";

	public static final Properties DEFAULT_SETTINGS = new Properties()
	{
		{
			setProperty( SK_HOSTNAME, "localhost" );
		}
	};

	protected Logger log;

	/**
	 * Creates a new reader with its own logger.
	 *
	 * The logger is an instance of Logger, from the Log4j package, that
	 * should be used in any subclasses of Reader to log the exceptions.
	 */
	public Reader()
	{
		log = Logging.getLogger( this );
	}

	/**
	 * Returns the next block to be parsed.
	 *
	 * A block might be an HTML page or any other instance of a document
	 * that is parsable by one of Unite's Parser implementations. The content
	 * is returned in the form of a Pair of String source content on the left
	 * and target content in the right.
	 *
	 * @return A text buffer Pair with one instance of the source and target
	 * content to be parsed. When either the source or target content is null,
	 * null is returned. Pair.NULL is returned when there's no more content
	 * to read.
	 */
	public abstract Pair next();

	/**
	 * Performs the close tasks required by the Reader.
	 *
	 * Closes file descriptors, databases and any other resources used by
	 * the reader.
	 */
	public abstract void close();
}
