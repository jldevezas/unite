package com.josedevezas.unite.similarity.util;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0420
 * @since 1.6
 */
public class EntitiySimilarity<E, S extends Comparable>
	implements Comparable<EntitiySimilarity>
{
	private E entity;
	private S similarity;

	public EntitiySimilarity( E entity, S similarity )
	{
		this.entity = entity;
		this.similarity = similarity;
	}

	public E getEntity()
	{
		return this.entity;
	}

	public S getSimilarity()
	{
		return this.similarity;
	}

	public int compareTo( EntitiySimilarity entitySimilarity )
	{
		return this.similarity.compareTo( entitySimilarity.getSimilarity() );
	}

	@Override
	public String toString()
	{
		return this.entity.toString() + ":" + this.similarity.toString();
	}
}
