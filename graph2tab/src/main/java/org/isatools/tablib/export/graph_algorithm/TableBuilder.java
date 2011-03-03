/**

 The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

 Exhibit A
 The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1

 "The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"). You may not use this file except in compliance with the License.
 You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

 The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
 2007-2011 ISA Team. All Rights Reserved.

 Contributor(s):
 Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
 Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
 supporting standards-compliant experimental annotation and enabling curation at the community level.
 Bioinformatics 2010;26(18):2354-6.

 Alternatively, the contents of this file may be used under the terms of either the GNU General
 Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
 http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
 or the LGPL are applicable instead of those above. If you wish to allow use of your version
 of this file only under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your decision by deleting
 the provisions above and replace them with the notice and other provisions required by the
 GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
 of this file under the terms of any one of the MPL, the GPL or the LGPL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
 (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
 (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

 */

package org.isatools.tablib.export.graph_algorithm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * The table builder. This is the thing to (possibly extend) and invoke to produce the table exported from the
 * experimental graph. This class invokes {@link ChainsBuilder} and produce a matrix of strings from the chains that
 * this class creates from the input graph.
 * <p/>
 * TODO: The columns are exported in no particular order, this occasionally screw the grouping of headers of the same
 * type (eg: factor value could fall between two characteristics column blocks). We will implement a solution to that in
 * future. For details about that, see inside {@link #addNode(int, Node)}.
 * <p/>
 * 
 * <dl>
 * <dt>date</dt>
 * <dd>May 10, 2010</dd>
 * </dl>
 * 
 * @author brandizi
 */
public class TableBuilder
{
	protected Set<Node> nodes;
	protected final boolean isLayeringRequired; 

	private LayersBuilder layersBuilder;
	private ChainsBuilder chainsBuilder;
	private LayerContents layerContents;
	protected List<List<String>> table = null;

	/**
	 * This defaults isLayeringRequired to true and keeps the nodes to work with uninitialised, you have to do it in your
	 * specific constructor. Hence, it is advisable that you setup this.nodes with the sinks after the call to this 
	 * constructor.
	 *  
	 */
	protected TableBuilder ()
	{
		this ( null, true );
	}

	/**
	 * This defaults isLayeringRequired to true, hence, it is advisable that you pass the sinks to this constructor.
	 */
	public TableBuilder ( Set<Node> nodes )
	{
		this ( nodes, true );
	}

	/**
	 * Nodes can be all the nodes in the graph or a subset like this:
	 *  
	 * <ul>
	 *   <li>in case isLayeringRequired = true, you can pass the sinks only, 
	 *   ie: all the right-most nodes, which have no output and allow you to reach the rest of the graph</li>
	 *   <li>in case isLayeringRequired = false, you can pass the sources only, ie those nodes on the left-most side of
	 *   the graph, which don't have inputs and allow you to reach the rest of the graph</li>
	 * </ul>
	 * 
	 * Passing the node subsets described will speed things up a little. 
	 * 
	 * @parameter isLayeringRequired true means that the graph may be uneven (with missing steps in the path from sources to sinks)
	 * and therefore it will require that layers are computed via {@link LayersBuilder}. Set this parameter to false
	 * <b>ONLY</b> if you are sure your graph has no such missing steps. 
	 */
	public TableBuilder ( Set<Node> nodes, boolean isLayeringRequired )
	{
		this.nodes = nodes;
		this.isLayeringRequired = isLayeringRequired;
	}

	
	/**
	 * The exported table, as a matrix of strings.
	 */
	public List<List<String>> getTable ()
	{
		if ( table != null )
			return table;

		if ( isLayeringRequired ) {
			layersBuilder = new LayersBuilder ( nodes );
			// It has the start nodes, so let's speed up things this way
			nodes = layersBuilder.getStartNodes ();
		}
		
		chainsBuilder = new ChainsBuilder ( nodes, layersBuilder );
		layerContents = new LayerContents ();

		for ( Node node: chainsBuilder.getStartNodes () )
		{
			int layer = 0, prevLayer = -1;
			while ( true )
			{
				if ( isLayeringRequired ) 
				{
					layer = layersBuilder.getLayer ( node );
					// Start from the previous'node layer and fill-in-the-blanks until you reach the current layer 
					for ( int layeri = prevLayer + 1; layeri < layer; layeri++ )
						layerContents.addNullRow ( layeri );
					prevLayer = layer;
				}

				if ( node.getTabValues ().isEmpty () )
					// Skip eg: processings not having at least one protocol
					layerContents.addNullRow ( layer );
				else
					addNode ( layer, node );
				
				SortedSet<Node> outs = node.getOutputs ();
				if ( outs.isEmpty () ) break;
				
				node = outs.first ();
				
				// This flag is final, so javac optimises a bit here
				if ( !isLayeringRequired ) layer++;
				
			} // while on chain nodes

			if ( isLayeringRequired )
			{
				// Fill-in-the-blanks until the last layer
				int maxLayer = layersBuilder.getMaxLayer ();
				for ( int layeri = prevLayer + 1; layeri <= maxLayer; layeri++ )
					layerContents.addNullRow ( layeri );
			}
		} // for chain
		
		table = new LayersListView ( layerContents );
		return table;
	}
	
	/**
	 * This add a node of the chains prepared by {@link ChainsBuilder} to the exported table. This is done by considering
	 * the pairs returned by {@link Node#getTabValues()}, values with the same header are merged.
	 */
	private void addNode ( int layer, Node node )
	{
		List<String> layerHeaders = layerContents.getLayerHeaders ( layer );

		int ncols = layerHeaders.size ();
		int nrows = layerContents.colsMaxSize ( layer );

		// First header of the group => indexes of columns where the header is
		Map<String, SortedSet<Integer>> headerIndexes = new HashMap<String, SortedSet<Integer>> ();

		// Initially it is like the layer contents
		for ( int icol = 0; icol < ncols; icol++ )
		{
			String header = layerHeaders.get ( icol );
			SortedSet<Integer> hidx = headerIndexes.get ( header );
			if ( hidx == null )
			{
				hidx = new TreeSet<Integer> ();
				headerIndexes.put ( header, hidx );
			}
			hidx.add ( icol );
		}

		// Work over all tab value groups
		//
		for ( TabValueGroup tbg: node.getTabValues () )
		{
			List<String> headers = tbg.getHeaders (), values = tbg.getValues ();
			int tbgSz = headers.size ();

			String header0 = headers.get ( 0 );
			SortedSet<Integer> hidx = headerIndexes.get ( header0 );

			if ( hidx == null )
			{
				// Header not appearing in current layer yet, to be added.
				//
				hidx = new TreeSet<Integer> ();
				for ( int itb = 0; itb < tbgSz; itb++ )
				{
					String header = headers.get ( itb );
					String value = values.get ( itb );
					int jcol = layerContents.addHeader ( layer, header ) - 1;
					// Only the first header in the group
					if ( itb == 0 )
						hidx.add ( jcol );
					layerContents.set ( layer, nrows, jcol, value );
				}
				continue;
			}

			// Header already used somewhere in the table being built.
			// Do we have a free cell in the existing columns for this header?
			//
			boolean isLanded = false;
			for ( int jcol: hidx )
			{
				if ( layerContents.get ( layer, nrows, jcol ) != null )
					continue;

				// There is a free cell for the current header, let's fill it with the corresponding object value and
				// go ahead
				for ( int itb = 0; itb < tbgSz; itb++ )
				{
					String value = values.get ( itb );
					layerContents.set ( layer, nrows, jcol++, value );
				}
				isLanded = true;
				break;
			}

			if ( isLanded )
				continue;

			// if !isLanded => cell is already taken for this header, we must add it to the layer columns and add the value
			// to the corresponding new position.
			// TODO: This screw the grouping of headers of the same type (eg: factor value could fall between two
			// characteristics
			// column blocks). To sort it out, we have to completely change the way the result is stored: header groups must
			// be stored explicitly, while currently we only store final headers, detached from their grouping.
			//
			for ( int itb = 0; itb < tbgSz; itb++ )
			{
				String header = headers.get ( itb );
				String value = values.get ( itb );

				int jcol = layerContents.addHeader ( layer, header ) - 1;
				layerContents.set ( layer, nrows, jcol, value );

				// Only the first header in the group, increment all the old headers on its left
				if ( itb == 0 )
				{
					for ( String idxh: headerIndexes.keySet () )
					{
						SortedSet<Integer> hidxNew = new TreeSet<Integer> ();
						if ( header0.equals ( idxh ) ) 
						{
							hidx = hidxNew;
							hidxNew.add ( jcol );
						}
						for ( int idxCol: headerIndexes.get ( idxh ) )
							hidxNew.add ( idxCol < jcol ? idxCol : idxCol + 1 );
						headerIndexes.put ( idxh, hidxNew );
					}
				}
			}
		}
	}

	
	/**
	 * @return A string representation of {@link #getTable()}, to be used for debugging puorposes.
	 */
	public String report ()
	{
		StringWriter sout = new StringWriter ();
		PrintWriter out = new PrintWriter ( sout );

		for ( List<String> row: getTable () )
		{
			for ( String v: row )
			{
				out.printf ( "%30.30s | ", v );
			}
			out.println ();
		}
		out.println ();
		return sout.toString ();
	}

}
