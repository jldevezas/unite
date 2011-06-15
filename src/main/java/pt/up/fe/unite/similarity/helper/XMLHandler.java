package pt.up.fe.unite.similarity.helper;

import java.util.Deque;
import java.util.Map;
import java.util.List;

import org.xml.sax.helpers.DefaultHandler;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0428
 * @since 1.6
 */
public abstract class XMLHandler extends DefaultHandler
{
	public abstract Deque<String> getKeys();
	public abstract boolean isUsingInternalCache();
	public abstract String getServer();
	public abstract int getPort();
	public abstract long getItemsCached();
	public abstract Map<String,List<Double>> getInternalCache();

	public void close() { }
}
