package pt.up.fe.unite;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.application.cli.Runner;
import pt.up.fe.unite.similarity.SimilarityGraphBuilder;
import pt.up.fe.unite.similarity.reader.XMLDescriptorReader;
import pt.up.fe.unite.similarity.writer.SimilarityGraphWriter;
import pt.up.fe.unite.similarity.writer.Neo4jSimilarityGraphWriter;
import pt.up.fe.unite.similarity.helper.XMLHandler;
import pt.up.fe.unite.similarity.helper.MarsupilamiIndexHandler;
import pt.up.fe.unite.mining.communities.CliquePercolation;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyIndexableGraph;
import com.tinkerpop.blueprints.pgm.oupls.jung.GraphJung;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.Pipeline;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.SingleIterator;
import com.tinkerpop.pipes.pgm.GraphElementPipe;
import com.tinkerpop.pipes.pgm.PropertyFilterPipe;
import com.tinkerpop.pipes.filter.FilterPipe;
import com.tinkerpop.pipes.filter.ComparisonFilterPipe;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.scoring.PageRank;

import java.util.Set;
import java.util.HashSet;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.ConsoleAppender;

import joptsimple.OptionSet;

/**
 * This is the main class, that will run the application.
 *
 * This class is used for our internal tests, but can be
 * used for your own applications, if desired.
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

	public void exampleWCCExtraction()
	{
		GraphJung graph = null;
		try
		{
			graph = new GraphJung( new ReadOnlyIndexableGraph(
					new Neo4jGraph( "/Users/jldevezas/Desktop/SAPOBlogGraph" ) ) );
			WeakComponentClusterer<Vertex,Edge> wcc = new WeakComponentClusterer<Vertex,Edge>();
			Set<Set<Vertex>> wccVertices = wcc.transform( graph );

			double averageComponentSize = 0;
			for ( Set component : wccVertices )
				averageComponentSize += component.size();
			averageComponentSize /= wccVertices.size();

			log.info( wccVertices.size() + " components found, with an average size of "
					+ averageComponentSize );
		}
		finally
		{
			graph.getGraph().shutdown();
		}
	}

	public void exampleEdgeBetweennessCommunities()
	{
		GraphJung graph = null;
		try
		{
			graph = new GraphJung(
					new Neo4jGraph( "/Users/jldevezas/Desktop/SAPOBlogGraph" ) );
			EdgeBetweennessClusterer<Vertex,Edge> ebc =
				new EdgeBetweennessClusterer<Vertex,Edge>( 10 );
			Set<Set<Vertex>> ebcVertices = ebc.transform( graph );

			BufferedWriter outFile = new BufferedWriter(
					new FileWriter( "/Users/jldevezas/Desktop/ebc.txt" ) );
			int i = 0;
			for ( Set<Vertex> community : ebcVertices )
			{
				outFile.newLine();
				outFile.write( "--------------------" );
				outFile.newLine();
				outFile.write( "Community " + i );
				outFile.newLine();
				outFile.write( "--------------------" );
				outFile.newLine();
				outFile.newLine();

				for ( Vertex vertex : community )
				{
					outFile.write( (String) vertex.getProperty( "url" ) );
					outFile.newLine();
				}
			}
		}
		catch ( IOException e )
		{
			log.error( "Input/output error", e );
		}
		finally
		{
			if ( graph != null ) graph.getGraph().shutdown();
		}
	}

	public void examplePageRank()
	{
		GraphJung graph = null;
		try
		{
			graph = new GraphJung(
					new Neo4jGraph( "/Users/jldevezas/Desktop/SAPOBlogGraph" ) );
			PageRank<Vertex,Edge> pr = new PageRank<Vertex,Edge>( graph, 0.85d );
			Index<Vertex> index = ( (IndexableGraph) graph.getGraph() )
				.getIndex( Index.VERTICES, Vertex.class );

			String nodeName = "fotos.sapo.pt";
			Vertex node = index.get( "url", nodeName ).iterator().next();

			log.info( "PageRank of node " + nodeName + " is " + pr.getVertexScore( node ) );
		}
		finally
		{
			if ( graph != null ) graph.getGraph().shutdown();
		}
	}

	class PropertyTailPipe
			extends AbstractPipe<Vertex,Vertex>
			implements FilterPipe<Vertex>
	{
		private String key;
		private String value;
		
		public PropertyTailPipe( String key, String value )
		{
			this.key = key;
			this.value = value;
		}

		public Vertex processNextStart()
		{
			while ( true )
			{
				Vertex vertex = this.starts.next();
				if ( ( (String) vertex.getProperty( key ) ).endsWith( value ) )
					return vertex;
			}
		}
	}

	public void exampleCliquePercolation( OptionSet options )
	{
		ReadOnlyIndexableGraph graph = null;
		
		try
		{
			graph = new ReadOnlyIndexableGraph(
				new Neo4jGraph( "/Users/jldevezas/Desktop/SAPOBlogGraph" ) );

			long k = 3;

			Pipe<Graph,Vertex> pipe1 = new GraphElementPipe( GraphElementPipe.ElementType.VERTEX );
			Pipe<Vertex,Vertex> pipe2 = new PropertyTailPipe( "url", "blogs.sapo.pt" );
			Pipe<Vertex,Vertex> pipe3 = new PropertyFilterPipe(
					"degree", k - 1, ComparisonFilterPipe.Filter.GREATER_THAN_EQUAL );
			Pipe<Graph,Vertex> pipeline = new Pipeline<Graph,Vertex>( pipe1, pipe2, pipe3 );
			pipeline.setStarts( new SingleIterator<Graph>( graph ) );

			HashSet<Vertex> allVertices = new HashSet<Vertex>();
			
			log.info( "Adding all vertices to set" );
			for ( Vertex vertex : pipeline )
			{
				if ( allVertices.add( vertex ) )
					log.trace( "Added vertex " + vertex.getProperty( "url" ) );
			}

			final HashSet<HashSet<Vertex>> resultSet =
				new HashSet<HashSet<Vertex>>();
			CliquePercolation cp = new CliquePercolation();
			cp.bronKerbosch( new HashSet<Vertex>(), allVertices, new HashSet<Vertex>() );
			cp.shutdown();
		}
		finally
		{
			if ( graph != null ) graph.shutdown();
		}
	}

	public static void exampleSimilarityGraph()
	{
		try
		{
			String inFile =
				"/home/jldevezas/data/marsupilami/index-small-sample.xml.gz";
				//"/Users/jldevezas/Desktop/Labs SAPO/Marsupilami/data/index.xml.gz";
			String outFile =
				"/home/jldevezas/simdb";
				//"/Users/jldevezas/Desktop/simdb";

			XMLHandler handler =
				new MarsupilamiIndexHandler( "nadir", 11211, true, true );
			XMLDescriptorReader reader = new XMLDescriptorReader( inFile, handler );
			reader.prepareCache();

			SimilarityGraphWriter writer = new Neo4jSimilarityGraphWriter( outFile );

			SimilarityGraphBuilder graphBuilder = new SimilarityGraphBuilder( reader, writer );
			graphBuilder.run();
		}
		catch ( Exception e )
		{
			log.error( e );
		}
	}

	public static void main( String[] args )
	{
		( new Runner( args ) ).run();
		//Logging.setup( Logging.DEBUG_PATTERN );
		//exampleSimilarityGraph();
	}
}
