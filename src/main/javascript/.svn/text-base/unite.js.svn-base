/*!
 * Unite Javascript Library 0.1
 * http://labs.sapo.pt/up
 * José Devezas (jld@fe.up.pt)
 */

/* <code-snippets> */

Array.prototype.clean = function( deleteValue )
{
	for ( var i = 0 ; i < this.length ; i++ )
	{
		if ( this[ i ] == deleteValue )
		{
			this.splice( i, 1 );
			i--;
		}
	}

	return this;
};

function getUrlVars()
{
	var vars = [], hash;
	var hashes = window.location.href.slice(
		window.location.href.indexOf( '?' ) + 1 ).split( '&' );

	for ( var i = 0; i < hashes.length; i++ )
	{
		hash = hashes[ i ].split( '=' );
		vars.push( hash[ 0 ] );
		vars[ hash[ 0 ] ] = hash[ 1 ];
	}

	return vars;
}

/* </code-snippets> */

function BlogInfo()
{
	var cache = {};
	var lastXhr = {};

	this.searchByDomain = function( request, response )
	{
		var term = request.term;

		if ( term in cache )
		{
			response( cache[ term ] );
			return;
		}

		lastXhr[ "searchByDomain" ] = $.getJSON(
			"http://nadir.fe.up.pt:8182/SAPOBlogGraph/indices/vertices",
			{ key : "url", value : "%query%" + term + "*" },
			function( data, status, xhr )
			{
				cache[ term ] = data;
			
				if ( xhr === lastXhr[ "searchByDomain" ] )
				{
					result = data.results.map( function( e )
					{
						var validDomain = "blogs.sapo.pt";
						if ( e.url.substr( - validDomain.length ) == validDomain )
							return { id : e.url, label : e.url, value : e.url };
					});

					result.clean( undefined );
					response( result );
				}
			});
	};
}

function CommunityVisualization( communities, elementId )
{
	var data = { nodes:[], links:[] };
	var nodeSet = {};
	var vis, force;
	var w = 960;
	var h = 600;
	var palette = d3.scale.category10();
	var fill = function( communities )
	{
		var color = palette( communities[ 0 ] );
		for ( var i = 1 ; i < communities.length ; i++ )
			color = d3.interpolateRgb( color, palette( i + 1 ) )( 0.5 );
		return color;
	};
	var radius = d3.scale.linear().domain([ 0, communities.length ]).range([ 5, 15 ]);

	var maxNodes = 100;

	this.display = function()
	{
		render( elementId );
		load();
	}

	var load = function()
	{
		for ( var i = 0 ; i < communities.length ; i++ )
		{
			$.getJSON( "http://nadir.fe.up.pt:8182/SAPOBlogGraph/indices/vertices",
				{ key : "communities", value : communities[ i ] },
				function( json )
				{
					for ( var j = 0 ; j < json.results.length ; j++ )
					{
						var nodeId = json.results[ j ]._id;
						var nodeName = json.results[ j ].url;
						var nodeCommunities = json.results[ j ].communities;
						var communityCount = 0;
						var nodeGroups = {};

						for ( var k = 0 ; k < communities.length ; k++ )
						{
							if ( nodeCommunities.indexOf( communities[ k ] ) != -1 )
								nodeGroups[ k + 1 ] = 1;
						}

						if ( ! nodeSet[ nodeId ] )
						{
							nodeSet[ nodeId ] = data.nodes.push({
								name: nodeName,
								groups: d3.keys( nodeGroups ),
							});
						}
					}

					if ( data.nodes.length > maxNodes )
					{
						$( "#dialog:ui-dialog" ).dialog( "destroy" );
						$( "#community-render-confirm" ).dialog({
							resizable: false,
							height:140,
							modal: true,
							buttons: {
								Yes: function() {
									$( this ).dialog( "close" );
									getEdges();
								},
								No: function() {
									render = false;
									$( this ).dialog( "close" );
								}
							}
						});
					}
					else
					{
						getEdges();
					}
				});
		}
	};

	var getEdges = function()
	{
		for ( var sourceNode in nodeSet )
		{
			$.getJSON( "http://nadir.fe.up.pt:8182/SAPOBlogGraph/vertices/" + sourceNode + "/inE",
				function( json )
				{
					for ( var i in json.results )
					{
						var targetNode = json.results[ i ]._outV;
						var edgeWeight = json.results[ i ].weight;
						if ( ! edgeWeight ) edgeWeight = 1;

						if ( targetNode in nodeSet )
						{
							data.links.push({
								source : nodeSet[ sourceNode ] - 1,
								target : nodeSet[ targetNode ] - 1,
								value : edgeWeight
							});
						}
					}

					update();
				});

			$.getJSON( "http://nadir.fe.up.pt:8182/SAPOBlogGraph/vertices/" + sourceNode + "/outE",
				function( json )
				{
					for ( var i in json.results )
					{
						var targetNode = json.results[ i ]._inV;
						var edgeWeight = json.results[ i ].weight;
						if ( ! edgeWeight ) edgeWeight = 1;

						if ( targetNode in nodeSet )
						{
							data.links.push({
								source : nodeSet[ sourceNode ] - 1,
								target : nodeSet[ targetNode ] - 1,
								value : edgeWeight
							});
						}
					}

					update();
				});
		}
	};

	var update = function()
	{
		var link = vis.selectAll("line.link")
				.data(data.links)
			.enter().append("svg:line")
				.attr("class", "link")
				.style("stroke-width", function(d) { return Math.sqrt(d.value); })
				.attr("x1", function(d) { return d.source.x; })
				.attr("y1", function(d) { return d.source.y; })
				.attr("x2", function(d) { return d.target.x; })
				.attr("y2", function(d) { return d.target.y; });

		var node = vis.selectAll("circle.node")
				.data(data.nodes)
			.enter().append("svg:circle")
				.attr("class", "node")
				.attr("cx", function(d) { return d.x; })
				.attr("cy", function(d) { return d.y; })
				.attr("r", function(d) { return radius(d.groups.length); })
				.style("fill", function(d) { return fill(d.groups); })
				.call(force.drag);

		node.append("svg:title")
				.text(function(d) { return d.name; });

		force.start();
	}

	var render = function()
	{
		vis = d3.select( elementId )
			.append("svg:svg")
				.attr("width", w)
				.attr("height", h);

		force = d3.layout.force()
				.nodes(data.nodes)
				.links(data.links)
				.size([w, h])
				.gravity(.1)
				.distance(100)
				.charge(-150)
				.start();

		vis.style("opacity", 1e-6)
			.transition()
				.duration(1000)
				.style("opacity", 1);

		force.on("tick", function() {
			vis.selectAll("line.link")
					.attr("x1", function(d) { return d.source.x; })
					.attr("y1", function(d) { return d.source.y; })
					.attr("x2", function(d) { return d.target.x; })
					.attr("y2", function(d) { return d.target.y; });

			vis.selectAll("circle.node")
					.attr("cx", function(d) { return d.x; })
					.attr("cy", function(d) { return d.y; });
		});
	};
}
