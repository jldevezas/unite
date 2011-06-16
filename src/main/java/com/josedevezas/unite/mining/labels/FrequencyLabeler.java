package com.josedevezas.unite.mining.labels;

import com.josedevezas.unite.util.Pair;
import com.josedevezas.unite.extraction.reader.Reader;
import com.josedevezas.unite.mining.labels.util.LabelScore;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import org.apache.lucene.util.Version;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0330
 * @since 1.6
 */
public class FrequencyLabeler extends Labeler
{
	protected HashMap<String,Integer> termsFrequency;
	private Analyzer analyzer;
	private HashSet<String> stopWords;

	public FrequencyLabeler( Reader reader )
	{
		super( reader );
		try
		{
			this.termsFrequency = new HashMap<String,Integer>();
			this.stopWords = new HashSet<String>(
					Arrays.asList( BrazilianAnalyzer.BRAZILIAN_STOP_WORDS ) );
			this.analyzer = new StandardAnalyzer( Version.LUCENE_30, stopWords );
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
	}

	public void parse( String content )
	{
		try
		{
			TokenStream tokenStream =
				analyzer.tokenStream( "content", new StringReader( content ) );

			while ( tokenStream.incrementToken() )
			{
				String word = tokenStream.getAttribute( TermAttribute.class ).term();
				Integer counter = termsFrequency.get( word );
				termsFrequency.put( word, counter == null ? 1 : counter + 1 );
			}
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
	}

	public List<String> getLabels( int maxLabels )
	{
		TreeSet<LabelScore<String,Float>> labelScores =
			new TreeSet<LabelScore<String,Float>>();

		for ( Map.Entry<String,Integer> entry : termsFrequency.entrySet() )
		{
			labelScores.add( new LabelScore<String,Float>(
						entry.getKey(), new Float( entry.getValue() ) ) );
		}

		ArrayList<String> labels = new ArrayList<String>();
		for ( int i = 0 ; i < labelScores.size() && i < maxLabels ; i++ )
		{
			labels.add( labelScores.pollFirst().getLabel() );
		}

		return labels;
	}
}
