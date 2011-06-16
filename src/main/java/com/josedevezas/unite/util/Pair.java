package com.josedevezas.unite.util;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0210
 * @since 1.6
 */
public class Pair<L,R>
{
	private final L left;
	private final R right;
	public static final Pair NULL = new Pair( null, null );

	public Pair( L left, R right )
	{
		this.left = left;
		this.right = right;
	}

	public L getLeft()
	{
		return this.left;
	}

	public R getRight()
	{
		return this.right;
	}
}
