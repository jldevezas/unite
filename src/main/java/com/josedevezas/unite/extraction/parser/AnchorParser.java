package com.josedevezas.unite.extraction.parser;

import java.util.List;
import java.util.LinkedList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class AnchorParser extends Parser
{
	private static final Pattern urlPattern =
		Pattern.compile( "<a.*?href=[\\\"\\\'](http[s]?://.*?)[\\\"\\\'].*?>" );

	public AnchorParser( String content )
	{
		super( content );
	}

	public List<String> parse()
	{
		List<String> result = new LinkedList<String>();

		Matcher matcher = urlPattern.matcher( content );
		while ( matcher.find() )
			result.add( matcher.group(1) );

		return result;
	}
}
