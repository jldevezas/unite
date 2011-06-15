package pt.up.fe.unite.mining.labels.util;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0323
 * @since 1.6
 */
public class LabelScore<L, S extends Comparable>
	implements Comparable<LabelScore>
{
	private L label;
	private S score;

	public LabelScore( L label, S score )
	{
		this.label = label;
		this.score = score;
	}

	public L getLabel()
	{
		return this.label;
	}

	public S getScore()
	{
		return this.score;
	}

	public int compareTo( LabelScore labelScore )
	{
		return this.score.compareTo( labelScore.getScore() );
	}
}
