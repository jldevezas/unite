package com.josedevezas.unite.extraction.parser;

import java.util.List;
import java.util.LinkedList;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class IdentityParser extends Parser
{
	public IdentityParser( String content )
	{
		super( content );
	}

	public List<String> parse()
	{
		List<String> result = new LinkedList<String>();
		result.add( content );
		return result;
	}
}
