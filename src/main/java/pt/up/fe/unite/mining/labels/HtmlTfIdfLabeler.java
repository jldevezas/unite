package pt.up.fe.unite.mining.labels;

import pt.up.fe.unite.extraction.reader.Reader;
import org.jsoup.Jsoup;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0330
 * @since 1.6
 */
public class HtmlTfIdfLabeler extends TfIdfLabeler
{
	public HtmlTfIdfLabeler( Reader reader )
	{
		super( reader );
	}

	public void parse( String content )
	{
		super.parse( Jsoup.parse( content ).text() );
	}
}
