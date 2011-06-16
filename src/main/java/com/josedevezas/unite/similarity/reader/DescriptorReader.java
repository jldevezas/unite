package com.josedevezas.unite.similarity.reader;

import com.josedevezas.unite.util.Logging;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0419
 * @since 1.6
 */
public abstract class DescriptorReader implements Cloneable
{
	protected Logger log;
	protected String id;

	public DescriptorReader()
	{
		this.log = Logging.getLogger( this );
		this.id = null;
	}

	public String getId()
	{
		return this.id;
	}

	public DescriptorReader clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException( "Clone method must be implemented" );
	}

	public abstract List<Double> next() throws Exception;

	public void close() { }
}
