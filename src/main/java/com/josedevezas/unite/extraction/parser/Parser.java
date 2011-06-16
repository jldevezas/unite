package com.josedevezas.unite.extraction.parser;

import com.josedevezas.unite.util.Logging;

import java.util.List;
import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public abstract class Parser
{
	protected Logger log;
	protected String content;

	/**
	 * Creates a new parser for the given content, with its own logger.
	 *
	 * The logger is an instance of Logger, from the Log4j package, that
	 * should be used in any subclasses of Parser to log the exceptions.
	 * Each Parser is associated with a content to be parsed, that is
	 * a Parser will extract information from a text, regarding either
	 * the set of source nodes or the set of target nodes.
	 *
	 * @param content a String with the text to be parsed.
	 * 
	 */
	public Parser( String content )
	{
		log = Logging.getLogger( this );
		content = content;
	}

	/**
	 * Implements a set of rules to parse the content.
	 *
	 * The parse method either looks for source nodes or target nodes
	 * present in the content of the Parser.
	 *
	 * @return Returns a list of String identifiers for either source
	 * nodes or target nodes.
	 */
	public abstract List<String> parse();
}
