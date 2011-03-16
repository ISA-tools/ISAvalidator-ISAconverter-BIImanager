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
 * <h2>The layer builder</h2>
 * 
 * <p>We try to explain below what the layering of an experimental work flow is, if you look at examples, one image
 * tells more than a thousand words. For instance, have a look at {@link LayeringTest#testUnevenGraph1()}</p>
 * 
 * <p>This is optionally used by {@link TableBuilder}. It is needed if you expect an input experimental workflow graph 
 * that may be "uneven", i.e.: it may contain paths from sources to end nodes of different length. This usually happen
 * because some step in the experimental workflow is missing or omitted (e.g.: a data item is achieved directly from a
 * sample, while another has a specified extract material and an extraction protocol). If that is the case, the graph 
 * need to be "layered", in addition to being transformed into a set of chains (by {@link ChainsBuilder}). For any 
 * node, an integer layer index is computed. The layers span from right to left, from sources to end nodes (or sinks).</p>
 *  
 * <p>For every chain from a source to a sink, there is at least one node per each layer and any given layer contain 
 * nodes of the same {@link Node#getType() node type}. In simpler cases there is exactly one node per layer for all 
 * such paths and, in this simple cases, the layer index is the same as the distance of the node from the source it 
 * can be reached from. In the case of uneven graphs, this basic layering is adjusted, so that every layer has always 
 * nodes of the same type.<p>  
 * 
 * <p>This basically uses the approach described in section 3.3.2 of the 
 * <a href = "http://annotare.googlecode.com/files/MAGE-TABv1.1.pdf">MAGE-TAB specification</a>, adding several details 
 * that are not addressed in that document, such as the usage of the {@link Node#getOrder() nodes' order property} to 
 * sort out ambiguities that may arise.</p>    
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
	
	/**
	 * Allows to know all the nodes in a given layer, which is needed for completing the layering computation.
	 */
	private SortedMap<Integer, SortedSet<Node>> layer2Nodes = new TreeMap<Integer, SortedSet<Node>> ();
	
	/**
	 * Allows to know the layer a node is associated to. This is initially null.
	 */
	private Map<Node, Integer> node2Layer = new HashMap<Node, Integer> (); 
	
	/**
	 * The max layer index that was computed.
	 */
	private int maxLayer = -1;
	
	/**
	 * You can pass any set of nodes that allow one to reach the sinks, i.e. the rightmost end nodes.
	 */
	public LayersBuilder ( Set<Node> nodes )
	{
		this.nodes = nodes;
	}

	/**
	 * Compute the end nodes by walking the graph.
	 */
	private void initEndNodes () 
	{
		endNodes = new HashSet<Node> ();
		for ( Node n: nodes ) initEndNodes ( n, endNodes );
	}
	
	/**
	 * recursion for {@link #initEndNodes()}.
	 *
	 */
	private void initEndNodes ( Node node, Set<Node> result ) 
	{
		if ( node.getOutputs ().isEmpty () ) {
			result.add ( node );
			return;
		}
		
		for ( Node out: node.getOutputs () ) 
			initEndNodes ( out, result );
		
		return;
	}
	
	/**
	 * Set the layer for a node, wich means all the two internal structures used for that are updated.
	 * 
	 */
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
	
	/**
	 * The first stage of the layering algorithm, layer indexes are computed by walking the graph upstream, i.e.: 
	 * layer ( n ) = max ( layer ( in ) ) for each in in {@link Node#getInputs()}. This is the recursive step.   
	 * 
	 */
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
	 * The first stage of the layering algorithm, layer indexes are computed by walking the graph upstream, i.e.: 
	 * layer ( n ) = max ( layer ( in ) ) for each in in {@link Node#getInputs()}.   
	 * 
	 */
	private void computeUntypedLayers ()
	{
		initEndNodes ();
		for ( Node sink: endNodes )
			computeUntypedLayer ( sink );
	}

	
	/**
	 * Computes the typed layers. It first invoked {@link #computeUntypedLayers()} and then walks all the layers, 
	 * adjusting all the nodes that have not the same types of the others. {@link Node#getOrder()} is used to determine
	 * which nodes have to shift on the right. Look at the code for details!
	 * 
	 */
	private void computeTypedLayers ()
	{
		computeUntypedLayers ();
		
		// Go through all the layers.
		for ( int layer = 0; layer < maxLayer; layer++ ) 
		{
			List<Node> layerNodes = new ArrayList<Node> ( layer2Nodes.get ( layer ) );
			int nn = layerNodes.size ();
			
			// All the node pairs in the layer
			for ( int i = 0; i < nn; i++ )
			{
				Node n = layerNodes.get ( i );
				// null means the node has gone to another layer
				if ( n == null ) continue; 
				
				for ( int j = i + 1; j < nn; j++ )
				{
					Node m = layerNodes.get ( j );
					// null means the node has gone to another layer
					if ( m == null ) continue;
					
					// Same type, they're OK, go ahead
					if ( n.getType().equals ( m.getType () ) ) continue;
					
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
						
						// All the nodes on L or R side (or both) are "doesn't matter" nodes, so let's shift the "good" node, based on
						// the assumption that it's more usual to specify a protocol's application output and omit the input, rather
						// than vice versa.
						// 
						if ( lefto == -1 || righto == -1 )
							shift2Right ( m, layerNodes, j );
						
						// Shift based on the side m is closer to, with respect to the layers preceding/following n
						// The rationale is that nodes like "Protocol REF" shift closer to their output side, reflecting the fact
						// that if one specifies a protocol omitting the input is more likely than omitting the output.
						// 
						// Note that when the node m is part of a chain of nodes having the same type (e.g.: sample1->sample2->sampl3
						// in one path and sample4->protocol1->sample5 in the other, where m = sample2 and n = protocol1)
						// it is that node (sample2), and not the other (protocol1), that is shifted. This is because likely
						// there are paths that didn't need multiple processing to elements of the same type (sample5 was 
						// likely obtained straight from sample4, without requiring an intermediate like sample2). Again, this is 
						// euristics based on experience with real use cases.
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
						// Move the node with greater order
						shift2Right ( m, layerNodes, j );
					else {
						// At this point we're sure that no > mo and they're != -1 
						shift2Right ( n );
						break;
					}
				} // for j
			} // for i
		} // for layer
		
		isInitialized = true;
	}

	/**
	 * Shifts the node to the right (i.e.: increase its layer index) and starts the propagation of that on the right side, 
	 * by invoking {@link #shift2Right(Node, List, int) shift2Right ( n, null, -1 )}.
	 * 
	 */
	private void shift2Right ( Node n ) {
		shift2Right ( n, null, -1 );
	}

	/**
	 * Shifts the node to the right (i.e.: increase its layer index) and starts the propagation of that on the right side,  
	 * by invoking {@link #shift2Right(Node, int, Set, List, int) shift2Right ( n, -1, new HashSet(), layerNodes, nodeIdx )}.
	 */
	private void shift2Right ( Node n, List<Node> layerNodes, int nodeIdx ) {
		shift2Right ( n, -1, new HashSet<Node> (), layerNodes, nodeIdx );
	}


	/**
	 * Shifts the node to the right (i.e.: increase its layer index) and recursively propagates that on the right side.
	 * visitedNodes allows it to stop the propagation on nodes that were already touched by this recursion. 

	 * @param n the node to be shifted, the method will recurse over n.getOutputs()
	 * @param prevNewLayer is the layer index that was computed by the previous recursive call (initially it is -1)
	 * @param visitedNodes allows it to stop the propagation on nodes that were already touched by this recursion
	 * @param layerNodes is the initial layer where the node is, if it is not null, the node will be removed from there. This
	 * parameter is sent here, cause {@link #computeTypedLayers()} needs to maintain an updated list of the nodes in the 
	 * layer it is being processing.
	 * @param nodeIdx is the initial layer index the node has i.e., this.layer2Nodes.get(nodeIdx) = n and it is used to
	 * remove the node from its original layer (if layerNodes != null). 
	 */
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
	
	/**
	 * The minimal order on the left side of the node. That is: 
	 * <ul>
	 *   <li>l0 = min ( nl.getOrder() != -1 and for all nl in m:layer(m) = layer(n) - 1)</li>
	 *   <li>if no node with order != -1 exist on the immediate left, layer(n) - 2,3,4... are evaluated the same way, 
	 *   the final result is -1 if we reach the leftmost side of the graph</li>
	 * </ul>
	 * 
	 * This means that we seek for the node having a significant order that is closer to the current node, if no such 
	 * node exists, we signal that by returning -1 (proper decisions are taken in {@link #computeTypedLayers()} in 
	 * such cases, see there).
	 *   
	 */
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
	
	/**
	 * Computes the minimal order on the right side of the node, using the same approach described in 
	 * {@link #minOrderLeft(Node)}. 
	 * 
	 */
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

	/**
	 * Exposes the layer index to the world.
	 */
	public int getLayer ( Node n ) 
	{
		if ( !isInitialized ) computeTypedLayers ();
		return node2Layer.get ( n );
	}
	
	/**
	 * Exposes the layer nodes to the world, the returned set is unmodifiable.
	 */
	public SortedSet<Node> getLayerNodes ( int layer ) 
	{
		if ( !isInitialized ) computeTypedLayers ();
		return Collections.unmodifiableSortedSet ( layer2Nodes.get ( layer ) );
	}

	/**
	 * The highest layer index that was computed, minimal is 0 and hence there are maxLayer + 1 layer, every layer has
	 * at least a node, which also means there is at least one source-to-sink path that touches it.
	 * 
	 */
	public int getMaxLayer ()
	{
		if ( !isInitialized ) computeTypedLayers ();
		return maxLayer;
	}

	/**
	 * Exposes the computed initial nodes to the world, they are passed to {@link ChainsBuilder}, faster than passing all 
	 * the graph. 
	 * 
	 */
	public SortedSet<Node> getStartNodes ()
	{
		if ( !isInitialized ) computeTypedLayers ();
		return Collections.unmodifiableSortedSet ( startNodes );
	}
	
	/**
	 * This is used by {@link ChainsBuilder}, that class transform the graph into a set of chains, by duplicating nodes and
	 * splitting their initial set of edges. This means that we need to save here the layering information for these 
	 * new nodes, which can be done via this method.
	 *   
	 */
	public void addSplittedNode ( Node original, Node newn ) 
	{
		Integer layer = node2Layer.get ( original );
		if ( layer == null ) throw new RuntimeException ( 
			"Internal Error: cannot insert a non-duped node in the layer builder, node: " + newn 
		);
		node2Layer.put ( newn, layer );
		layer2Nodes.get ( layer ).add ( newn );
	}
	
	/**
	 * A representation of the current graph layering, useful for debugging. 
	 */
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
