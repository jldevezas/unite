package com.josedevezas.unite.application.service.result;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author José Devezas (jld@fe.up.pt)
 * @version 2011.0413
 * @since 1.6
 */
@XmlRootElement( name="node-rank" )
public class NodeRankResult
{
	public String method;
	public Long limit;
	
	public NodeRankResult() {}

	public NodeRankResult( String method, Long limit )
	{
		this.method = method;
		this.limit = limit;
	}
}
