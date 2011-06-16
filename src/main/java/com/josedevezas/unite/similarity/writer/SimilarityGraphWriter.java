package com.josedevezas.unite.similarity.writer;

import com.josedevezas.unite.util.Logging;
import com.josedevezas.unite.similarity.util.EntitiySimilarity;

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
