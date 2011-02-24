package org.isatools.tablib.export.graph_algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * TODO: Comment me thoroughly!!! 
 * 
 * <dl><dt>date</dt><dd>Feb 23, 2011</dd></dl>
 * @author brandizi
 *
 */
public class LayersBuilder
{
	private boolean isInitialized = false;
	
	private Set<Node> nodes;
	private Set<Node> endNodes = null;
	private SortedSet<Node> startNodes = new TreeSet<Node> ();
	
	private SortedMap<Integer, SortedSet<Node>> layer2Nodes = new TreeMap<Integer, SortedSet<Node>> ();
	private Map<Node, Integer> node2Layer = new HashMap<Node, Integer> (); 
	int maxLayer = -1;
	
	public LayersBuilder ( Set<Node> nodes )
	{
		this.nodes = nodes;
	}
	
	private void initEndNodes () 
	{
		if ( endNodes != null ) return;
		
		endNodes = new HashSet<Node> ();
		for ( Node n: nodes )
			if ( n.getInputs ().isEmpty () )
				endNodes.add ( n );
	}
	
	
	private void setLayer ( Node node, int layer ) 
	{
		// Remove from the old layer
		Integer oldLayer = node2Layer.get ( node );
		if ( oldLayer != null ) {
			layer2Nodes.get ( oldLayer ).remove ( node );
		}
		
		// Add to the new layer
		SortedSet<Node> lnodes = layer2Nodes.get ( layer );
		if ( lnodes == null ) {
			lnodes = new TreeSet<Node> ();
			layer2Nodes.put ( layer, lnodes );
		}
		lnodes.add ( node );
		node2Layer.put ( node, layer );
		if ( layer > maxLayer ) maxLayer = layer;
	}
	
	private int computeUntypedLayer ( Node node )
	{
		Integer result = node2Layer.get ( node );
		if ( result != null ) return result;
		
		result = -1;
		SortedSet<Node> ins = node.getInputs ();
		if ( ins.isEmpty () ) startNodes.add ( node );
		
		for ( Node in: node.getInputs () )
		{
			int il = computeUntypedLayer ( in );
			if ( result < il ) result = il;
		}
		setLayer ( node, ++result );
		return result;
	}

	/**
	 * It's simply n.getTabValues ().get ( 0 ).getHeaders ().get ( 0 ), i.e., the first header, which is something like
	 * "Source Name" or "Data File Name". 
	 *  
	 */
	private String getType ( Node n )
	{
		return n.getTabValues ().get ( 0 ).getHeaders ().get ( 0 );
	}
	
	private void computeUntypedLayers ()
	{
		initEndNodes ();
		for ( Node sink: endNodes )
			computeUntypedLayer ( sink );
	}

	
	private void computeTypedLayers ()
	{
		computeUntypedLayers ();
		
		for ( int layer = 0; layer < maxLayer; layer++ ) 
		{
			List<Node> layerNodes = new ArrayList<Node> ( layer2Nodes.get ( layer ) );
			int nn = layerNodes.size ();
			
			for ( int i = 0; i < nn; i++ )
			{
				Node n = layerNodes.get ( i );
				for ( int j = i + 1; j < nn; j++ )
				{
					Node m = layerNodes.get ( j );
					// null means the node has gone to another layer
					if ( m == null ) continue;
					if ( getType ( n ).equals ( getType ( m ) ) ) continue;
					
					int no = n.getOrder (), mo = m.getOrder ();

					if ( no == -1 && mo == -1 ) 
					{
						// Two nodes which of order doesn't matter, do a conventional move
						shift2Right ( m, layerNodes, j );
					}
					else if ( no == -1 ) 
					{
						// The minimal order that is on our right and on our left
						int lefto = minOrderLeft ( n );
						int righto = minOrderRight ( n );
						
						// All the nodes on L/R side (or both) are "doesn't matter" nodes, so let's shift the "good" node, based on
						// the assumption that it's more usual to specify a protocol's application output and omit the input, rather
						// than vice versa.
						// 
						if ( lefto == -1 || righto == -1 )
							shift2Right ( m, layerNodes, j );
						
						// Shift based on the side m is closer to, with respect to the layers preceding/following n
						// The rationale is that nodes like "Protocol REF" shift closer to their output side, reflecting the fact
						// that if one specifies a protocol omitting the input is more likely than omitting the output.
						//
						if ( righto - mo <= mo - lefto )
							shift2Right ( m, layerNodes, j );
						else {
							shift2Right ( n );
							break;
						}
					}
					else if ( mo == -1 )
					{
						// Do the same as above, but with the role of n and m swapped
						// 
						int lefto = minOrderLeft ( m );
						int righto = minOrderRight ( m );

						if ( lefto == -1 || righto == -1 ) {
							shift2Right ( n );
							break;
						}

						if ( righto - no <= no - lefto )
						{
							shift2Right ( n );
							break;
						}
						else
							shift2Right ( m, layerNodes, j );
					}
					else if ( no <= mo )
						shift2Right ( m, layerNodes, j );
					else {
						shift2Right ( n );
						break;
					}
				} // for j
			} // for i
		} // for layer
		
		isInitialized = true;
	}



	private void shift2Right ( Node n ) {
		shift2Right ( n, null, -1 );
	}

	private void shift2Right ( Node n, List<Node> layerNodes, int nodeIdx ) {
		shift2Right ( n, -1, new HashSet<Node> (), layerNodes, nodeIdx );
	}

	private void shift2Right ( Node n, int prevNewLayer, Set<Node> visitedNodes, List<Node> layerNodes, int nodeIdx )
	{
		// Visited, give up
		if ( visitedNodes.contains ( n ) ) return;
		
		int oldlayer = node2Layer.get ( n );

		// The previous shift had enough room, give up
		if ( prevNewLayer != -1 &&  oldlayer - prevNewLayer > 0 ) return;
		
		setLayer ( n, ++oldlayer );
		visitedNodes.add ( n );
		
		for ( Node out: n.getOutputs () )
			shift2Right ( out, oldlayer, visitedNodes, null, -1 );
		
		if ( layerNodes != null )
			layerNodes.set ( nodeIdx, null );
	}
	
	
	private int minOrderLeft ( Node n )
	{
		int nl = node2Layer.get ( n );

		while ( --nl >= 0 ) 
		{
			SortedSet<Node> lNodes = layer2Nodes.get ( nl );
			if ( !lNodes.isEmpty () ) 
			{ 
				int lno = lNodes.first ().getOrder ();
				// Ignore "doesn't matter" nodes
				if ( lno > -1 ) return lno;  
			}
		}
		return -1;
	}
	

	private int minOrderRight ( Node n )
	{
		int nl = node2Layer.get ( n );
		
		while ( ++nl <= maxLayer ) 
		{
			SortedSet<Node> rNodes = layer2Nodes.get ( nl );
			if ( !rNodes.isEmpty () ) 
			{
				int result = -1;
				for ( Node rnode: rNodes ) 
				{
					int ro = rnode.getOrder ();
					// Ignore "doesn't matter" nodes and keep searching until you find "good" nodes
					if ( ro < 0 ) continue;
					if ( result == -1 || ro < result ) result = ro;
				}
				// Ignore "doesn't matter" nodes and keep going right until you find "good" nodes
				if ( result > -1 ) return result;
			}
		}
		return -1;
	}

	
	public int getLayer ( Node n ) 
	{
		if ( !isInitialized ) computeTypedLayers ();
		return node2Layer.get ( n );
	}
	
	public SortedSet<Node> getLayerNodes ( int layer ) 
	{
		if ( !isInitialized ) computeTypedLayers ();
		return Collections.unmodifiableSortedSet ( layer2Nodes.get ( layer ) );
	}
	
	public SortedSet<Node> getStartNodes ()
	{
		if ( !isInitialized ) computeTypedLayers ();
		return Collections.unmodifiableSortedSet ( startNodes );
	}
	
	
	
	@Override
	public String toString ()
	{
		String result = ""; 
		for ( int layer: layer2Nodes.keySet () )
		{
			result += "LAYER " + layer + ":\n";
			result += "  ";
			for ( Node n: layer2Nodes.get ( layer ) )
				result += n + "  ";
			result += "\n\n";
		}
		
		return result;
	}

}
