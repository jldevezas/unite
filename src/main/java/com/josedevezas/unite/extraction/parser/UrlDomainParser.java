package com.josedevezas.unite.extraction.parser;

import java.util.List;
import java.util.LinkedList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0225
 * @since 1.6
 */
public class UrlDomainParser extends Parser
{
	private static final Pattern domainPattern =
		Pattern.compile( "http[s]?://([^/?#\\\"']+)" );

	public UrlDomainParser( String content )
	{
		super( content );
	}

	public List<String> parse()
	{
		List<String> result = new LinkedList<String>();

		Matcher matcher = domainPattern.matcher( content );
		while ( matcher.find() )
		{
			log.trace( matcher.group( 1 ) );
			result.add( matcher.group( 1 ) );
		}

		return result;
	}
}
