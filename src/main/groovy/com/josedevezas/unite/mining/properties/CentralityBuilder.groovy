package pt.up.fe.unite.mining.properties

import pt.up.fe.unite.mining.properties.PropertyBuilder

import java.util.Map
import java.util.Properties

import com.tinkerpop.gremlin.Gremlin
import com.tinkerpop.blueprints.pgm.Vertex

class CentralityBuilder extends PropertyBuilder
{
	static
	{
		Gremlin.load()
	}

	CentralityBuilder( Properties settings )
	{
		super( settings )
	}
	
	void run()
	{
		int c = 0;
		Map<Vertex,Long> scores = [:]

		log.info( "Calculating the eigenvector centrality" )
		graph.V.outE.inV.groupCount( scores ).loop( 3 ) { c++ < 1000 } >> -1
		scores.each { vertex, score -> vertex.eigenvectorCentrality = score }
	}
}
