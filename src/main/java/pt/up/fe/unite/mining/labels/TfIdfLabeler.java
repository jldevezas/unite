package pt.up.fe.unite.mining.labels;

import pt.up.fe.unite.extraction.reader.Reader;
import pt.up.fe.unite.mining.labels.util.LabelScore;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.util.Version;
import org.apache.lucene.store.RAMDirectory;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0408
 * @since 1.6
 */
public class TfIdfLabeler extends Labeler
{
	private IndexWriter indexWriter;
	private RAMDirectory index;
	private Analyzer analyzer; 
	private HashSet<String> stopWords;

	public TfIdfLabeler( Reader reader )
	{
		super( reader );
		
		try
		{
			this.index = new RAMDirectory();
			this.stopWords = new HashSet<String>(
					Arrays.asList( BrazilianAnalyzer.BRAZILIAN_STOP_WORDS ) );
			this.analyzer = new StandardAnalyzer( Version.LUCENE_30, stopWords );
			this.indexWriter = new IndexWriter( index, analyzer, true,
					IndexWriter.MaxFieldLength.UNLIMITED );
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
	}

	protected void parse( String content )
	{
		try
		{
			Document document = new Document();
			document.add( new Field( "content", content, Field.Store.YES,
						Field.Index.ANALYZED, Field.TermVector.YES ) );
			indexWriter.addDocument( document );
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
	}

	@Override protected void close()
	{
		try
		{
			indexWriter.close();
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}
	}

	public List<String> getLabels( int maxLabels )
	{
		HashMap<String,ArrayList<Float>> termsTfIdf = new HashMap<String,ArrayList<Float>>();
		DefaultSimilarity similarity = new DefaultSimilarity();

		try
		{
			IndexSearcher indexSearcher = new IndexSearcher( index, true );
			IndexReader indexReader = indexSearcher.getIndexReader();

			for ( int i = 0 ; i < indexReader.maxDoc() ; i++ )
			{
				if ( indexReader.isDeleted( i ) ) continue;

				TermFreqVector termFreqVector = indexReader.getTermFreqVector( i, "content" );
				if ( termFreqVector == null ) continue;

				int[] termFrequencies = termFreqVector.getTermFrequencies();
				String[] terms = termFreqVector.getTerms();

				for ( int j = 0 ; j < termFrequencies.length ; j++ )
				{
					ArrayList<Float> tfIdfList = termsTfIdf.get( terms[ j ] );
					if ( tfIdfList == null )
					{
						tfIdfList = new ArrayList<Float>();
						termsTfIdf.put( terms[ j ], tfIdfList );
					}

					tfIdfList.add( new Float(
								similarity.tf( termFrequencies[ j ] ) *
								similarity.idf(
									indexSearcher.docFreq( new Term( "content", terms[ j ] ) ),
									indexReader.numDocs() ) ) );
				}
			}
		}
		catch ( Exception e )
		{
			log.error( e.getMessage() );
		}

		TreeSet<LabelScore<String,Float>> labelScores =
			new TreeSet<LabelScore<String,Float>>();

		log.trace( termsTfIdf );
		for ( Map.Entry<String,ArrayList<Float>> entry : termsTfIdf.entrySet() )
		{
			Float tfIdfAverage = new Float( 0 );
			ArrayList<Float> tfIdfList = entry.getValue();

			for ( Float tfIdf : tfIdfList )
				tfIdfAverage += tfIdf;
			tfIdfAverage /= tfIdfList.size();

			labelScores.add( new LabelScore<String,Float>( entry.getKey(), tfIdfAverage ) );
		}

		ArrayList<String> labels = new ArrayList<String>();
		for ( int i = 0 ; i < labelScores.size() && i < maxLabels ; i++ )
		{
			String label = labelScores.pollFirst().getLabel();
			labels.add( label );
		}

		return labels;
	}
}
