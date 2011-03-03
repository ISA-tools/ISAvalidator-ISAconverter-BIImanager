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

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.utils.collections.ListUtils;
import uk.ac.ebi.utils.collections.SortedObjectStore;

import java.util.*;


/**
 * Provides a representation of the layers in the graph. A layer is a set of homogeneous nodes having the same distance
 * from the sources (eg: the initial column of sources or all the samples after the source column).
 * <p/>
 * Basically we allow to represent the structure &lt;layer no., row, column (in the header)&gt; =&gt; value
 * <p/>
 * <dl>
 * <dt>date</dt>
 * <dd>May 5, 2010</dd>
 * </dl>
 * 
 * @author brandizi
 */
class LayerContents
{
	/**
	 * All the contents
	 */
	private final Map<Integer, LayerContent> layerContents = new HashMap<Integer, LayerContent> ();

	/**
	 * Counts the overall number of rows and columns
	 */
	private int nrows = 0, ncols = 0;

	/**
	 * The contents of a single layer, which is like a table. For efficiency, we store the table by using a coordinate
	 * system, ie a double map like [col,row] =&gt; value. Moreover, we maintain the headers separately and some cache
	 * information (no of rows in each colum).
	 */
	private static class LayerContent
	{
		/**
		 * The headers, indexed by column
		 */
		final List<String> headers = new ArrayList<String> ();
		/**
		 * first key is the column and second is the row, most of times we work on the table by considering the column first
		 */
		final SortedObjectStore<Integer, Integer, String> table = new SortedObjectStore<Integer, Integer, String> ();
		/**
		 * we maintain the number of rows in each column, which is computed from the max column index set in the table
		 * structure
		 */
		List<Integer> colSizes = new ArrayList<Integer> ();
		/**
		 * max number of rows in this layer
		 */
		int colsMaxSize = 0;
	}

	/**
	 * The list of layers which were set, in no particular order
	 */
	public Set<Integer> getLayers ()
	{
		return layerContents.keySet ();
	}

	private LayerContent getLayerContent ( int layer )
	{
		LayerContent layerCont = layerContents.get ( layer );
		if ( layerCont != null )
		{
			return layerCont;
		}
		layerCont = new LayerContent ();
		layerContents.put ( layer, layerCont );
		return layerCont;
	}

	/**
	 * The header for the given column and layer. icol must be &lt; {@link #headersSize(int) headersSize(layer)}
	 */
	public String setHeader ( int layer, int icol, String header )
	{
		List<String> lheaders = getLayerContent ( layer ).headers;
		if ( icol >= lheaders.size () )
		{
			throw new IndexOutOfBoundsException ( "while setting '" + header + "' at " + icol );
		}
		ncols++;
		return lheaders.set ( icol, header );
	}

	/**
	 * the header for this layer and col, icol must be &lt; {@link #headersSize(int) headersSize(layer)}
	 */
	public String getHeader ( int layer, int icol )
	{
		List<String> lheaders = getLayerContent ( layer ).headers;
		if ( icol >= lheaders.size () )
		{
			throw new IndexOutOfBoundsException ( "while asking header at " + icol );
		}
		return ListUtils.get ( lheaders, icol );
	}

	/**
	 * Adds up an header to the layer
	 * 
	 * @return the new size of the layer
	 */
	public int addHeader ( int layer, String header )
	{
		ncols++;
		LayerContent layerCont = getLayerContent ( layer );
		List<String> lheaders = layerCont.headers;
		lheaders.add ( header );
		layerCont.colSizes.add ( 0 );
		return lheaders.size ();
	}

	/**
	 * Add all the headers
	 */
	public void addAllHeaders ( int layer, Collection<String> newHeaders )
	{
		if ( newHeaders == null )
		{
			return;
		}
		LayerContent layerCont = getLayerContent ( layer );
		int newHsz = newHeaders.size ();
		List<String> lheaders = layerCont.headers;
		lheaders.addAll ( newHeaders );
		for ( int i = 0; i < newHsz; i++ )
		{
			layerCont.colSizes.add ( 0 );
		}
		ncols += newHsz;
	}

	
	/**
	 * Adds an header in the icol position an shifts all on the right, like in {@link List#add(int, Object)}
	 */
	public int addHeader ( int layer, int icol, String header )
	{
		LayerContent layerCont = getLayerContent ( layer );
		List<String> lheaders = layerCont.headers;
		if ( icol > lheaders.size () )
		{
			throw new IndexOutOfBoundsException ( "while adding '" + header + "' at " + icol );
		}

		// Move the values too
		//
		SortedObjectStore<Integer, Integer, String> table = layerCont.table;
		for ( int idxCol = lheaders.size (); idxCol > icol; idxCol-- )
		{
			int idxColm1 = idxCol - 1;
			Set<Integer> oldvals = table.typeKeys ( idxCol );
			if ( oldvals != null )
			{
				for ( int irow: oldvals )
				{
					layerCont.table.put ( idxCol, irow, "" );
				}
			}

			for ( int irow: table.typeKeys ( idxColm1 ) )
			{
				layerCont.table.put ( idxCol, irow, table.get ( idxColm1, irow ) );
			}
		}
		table.remove ( icol );
		//

		ncols++;
		lheaders.add ( icol, header );
		layerCont.colSizes.add ( icol, 0 );
		return lheaders.size ();
	}


	/**
	 * The headers for this layer
	 */
	public List<String> getLayerHeaders ( int layer )
	{
		List<String> lheaders = getLayerContent ( layer ).headers;
		return Collections.unmodifiableList ( lheaders );
	}

	/**
	 * Update the several counters in the {@link LayerContent} class and in this class
	 */
	private void updatedColSize ( LayerContent layerCont, int icol, int newSize )
	{
		if ( newSize <= layerCont.colSizes.get ( icol ) )
		{
			return;
		}
		layerCont.colSizes.set ( icol, newSize );
		if ( newSize <= layerCont.colsMaxSize )
		{
			return;
		}
		layerCont.colsMaxSize = newSize;
		if ( newSize <= nrows )
		{
			return;
		}
		nrows = newSize;
	}

	// private int incColSize ( LayerContent layerCont, int icol )
	// {
	// int oldSize = layerCont.colSizes.get ( icol ), newSize = oldSize + 1;
	// layerCont.colSizes.set ( icol, newSize );
	// if ( newSize <= layerCont.colsMaxSize ) return oldSize;
	// layerCont.colsMaxSize = newSize;
	// if ( newSize <= nrows ) return oldSize;
	// nrows = newSize;
	// return oldSize;
	// }

	/**
	 * The value for a given cell in a certain layer. irow can be any positive integer, the table will be extended as
	 * needed. icol must be &lt {@link #headersSize(int)}
	 */
	public void set ( int layer, int irow, int icol, String v )
	{
		LayerContent layerCont = getLayerContent ( layer );
		SortedObjectStore<Integer, Integer, String> table = layerCont.table;

		if ( icol >= layerCont.headers.size () )
		{
			throw new IndexOutOfBoundsException ( "while setting '" + v + "' at " + layer + ", " + icol + "," + irow );
		}

		table.put ( icol, irow, v );
		updatedColSize ( layerCont, icol, irow + 1 );
	}

	/**
	 * The value for a given cell in a certain layer. icol must be &lt {@link #headersSize(int)}, irow can be any positive
	 * integer, nulls will be returned in case out-of-bound coordinates are passed
	 */
	public String get ( int layer, int irow, int icol )
	{
		LayerContent layerCont = getLayerContent ( layer );
		if ( icol >= layerCont.headers.size () )
		{
			throw new IndexOutOfBoundsException ( "while getting " + layer + ", " + icol + "," + irow );
		}

		return StringUtils.trimToNull ( layerCont.table.get ( icol, irow ) );
	}

	// public void add ( int layer, int icol, String v )
	// {
	// LayerContent layerCont = getLayerContent ( layer );
	// if ( icol >= layerCont.headers.size () )
	// throw new IndexOutOfBoundsException ( "while adding '" + v + "' at " + layer + ", " + icol );
	//
	// SortedObjectStore<Integer, Integer, String> table = layerCont.table;
	// int oldSize = incColSize ( layerCont, icol );
	// if ( v != null ) table.put ( icol, oldSize, v );
	// }

	/**
	 * TODO: Comment me!
	 */
	public void addNullRow ( int layer )
	{
		LayerContent layerCont = getLayerContent ( layer );
		if ( layerCont.headers.size () == 0 )
		{
			// It has not been populated with real nodes yet (we're skipping some layers): just set the row counters and it 
			// will be enough
			if ( ++layerCont.colsMaxSize > nrows ) nrows++;
			return;
		}
		
		// It's a null node, so it's like to add a null value to the first header.
		set ( layer, layerCont.colsMaxSize, 0, null );
	}
	
	
	/**
	 * The number of columns/headers for this layer
	 */
	public int headersSize ( int layer )
	{
		return getLayerContent ( layer ).headers.size ();
	}

	/**
	 * The max number of rows in all the contents. You should see the final structure as a table having the same number of
	 * rows for every column.
	 */
	public int headersMaxSize ()
	{
		return ncols;
	}

	/**
	 * The max number of rows in this column and layer. This is 1 + the max row that was passed by
	 * {@link #set(int, int, int, String)}
	 */
	public int colSize ( int layer, int icol )
	{
		LayerContent layerCont = getLayerContent ( layer );
		return layerCont.colSizes.get ( icol );
	}

	/**
	 * The max number of rows over all the columns of this layer. This is obviously max ( {@link #colSize(int, int)} ),
	 * but it's cached, not computed by this method at every call.
	 */
	public int colsMaxSize ( int layer )
	{
		LayerContent layerCont = getLayerContent ( layer );
		return layerCont.colsMaxSize;
	}

	/**
	 * The max number of of rows over all the contents. This is obviously max ( {@link #colsMaxSize(int)} ), but it's
	 * cached, not computed by this method at every call.
	 */
	public int colsMaxSize ()
	{
		return nrows;
	}

	/**
	 * reset all the contents
	 */
	public void clear ()
	{
		layerContents.clear ();
		nrows = ncols = 0;
	}

	/**
	 * @return report of current contents
	 * 
	 */
	@Override
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ();
		sb.append ( "-- ROWS: " + this.nrows + ", COLS: " + this.ncols + "\n\n");
		for ( int layer: layerContents.keySet () )
		{
			LayerContent layerCont = layerContents.get ( layer );
			sb.append ( "---- LAYER: " + layer + ", colsMaxSize = " + layerCont.colsMaxSize + "\n" );
			int ncols = layerCont.headers.size ();
			if ( ncols == 0 )
				sb.append ( "  --empty--" );
			else
			{
				sb.append ( "\t\t" );
				for ( int col = 0; col < ncols; col++ )
					sb.append ( col + ": " + layerCont.headers.get ( col ) + "\t\t" );
				sb.append ( "\n" );
				for ( int row = 0; row < layerCont.colsMaxSize; row++ )
				{
					sb.append ( row + ":\t\t" );
					for ( int col = 0; col < ncols; col++ )
						sb.append ( col + ": " + get ( layer, row, col ) + "\t\t" );
					sb.append ( "\n" );
				}
			}
			sb.append ( "\n" );
		}
		return sb.toString ();
	}
	
}
