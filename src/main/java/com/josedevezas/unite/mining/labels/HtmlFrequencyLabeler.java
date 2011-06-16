package com.josedevezas.unite.mining.labels;

import com.josedevezas.unite.extraction.reader.Reader;
import org.jsoup.Jsoup;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0330
 * @since 1.6
 */
public class HtmlFrequencyLabeler extends FrequencyLabeler
{
	public HtmlFrequencyLabeler( Reader reader )
	{
		super( reader );
	}

	public void parse( String content )
	{
		super.parse( Jsoup.parse( content ).text() );
	}
}
