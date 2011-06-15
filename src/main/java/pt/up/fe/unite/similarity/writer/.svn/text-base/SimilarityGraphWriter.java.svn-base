package pt.up.fe.unite.similarity.writer;

import pt.up.fe.unite.util.Logging;
import pt.up.fe.unite.similarity.util.EntitiySimilarity;

import java.util.Collection;

import org.apache.log4j.Logger;

public abstract class SimilarityGraphWriter
{
	protected Logger log;

	public SimilarityGraphWriter()
	{
		log = Logging.getLogger( this );
	}

	public abstract void addNeighbors(
			String source, Collection<EntitiySimilarity> targets );

	public abstract void close();
}
