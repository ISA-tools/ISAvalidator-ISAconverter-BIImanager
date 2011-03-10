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

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * A default skeleton implementation of {@link Node}. You'll probably want to implement your customized version of the
 * experimental node starting from here, cause we provide a convenient implementation of {@link #equals(Object)} and
 * {@link #hashCode()} that make every new node distinct. For the same reasons, you'll probably want to create nodes via
 * a specific implementation of {@link AbstractNodeFactory}.
 * <p/>
 * <dl>
 * <dt>date</dt>
 * <dd>May 10, 2010</dd>
 * </dl>
 * 
 * @author brandizi
 */
public abstract class DefaultAbstractNode implements Node
{
	private static long size = 0;

	/**
	 * Update these for storing this node's links. You should do that only in {@link #getInputs()} and
	 * {@link #getOutputs()}.
	 */
	protected SortedSet<Node> inputs = null, outputs = null;

	/**
	 * We use this to assign a unique identifier to each node and apply it to compute {@link #compareTo(LabeledNode)}
	 */
	private final long id;

	protected DefaultAbstractNode ()
	{
		synchronized ( DefaultAbstractNode.class )
		{
			if ( size == Long.MAX_VALUE )
			{
				throw new RuntimeException ( "No more integers for generating nodes!" );
			}
			id = size++;
		}
	}

	/**
	 * By default it returns an unmodifiable version of {@link #inputs}. You should implement something like: if ( ! null
	 * ) return {@link #inputs} else generate inputs (eg: from wrappers, via your implementation of
	 * {@link AbstractNodeFactory}).
	 */
	public SortedSet<Node> getInputs ()
	{
		return Collections.unmodifiableSortedSet ( inputs );
	}

	/**
	 * By default it returns an unmodifiable version of {@link #inputs}. You should implement something like: if ( ! null
	 * ) return {@link #outputs} else generate inputs (eg: from wrappers, via your implementation of
	 * {@link AbstractNodeFactory}).
	 */
	public SortedSet<Node> getOutputs ()
	{
		return Collections.unmodifiableSortedSet ( outputs );
	}

	/**
	 * This is the deafult implementation that works with {@link #inputs}. You should not need to touch it.
	 */
	public boolean addInput ( Node input )
	{
		if ( inputs == null )
		{
			getInputs ();
		}
		if ( !inputs.add ( input ) )
		{
			return false;
		}
		input.addOutput ( this );
		return true;
	}

	/**
	 * This is the deafult implementation that works with {@link #inputs}. You should not need to touch it.
	 */
	public boolean addOutput ( Node output )
	{
		if ( outputs == null )
		{
			getOutputs ();
		}
		if ( !outputs.add ( output ) )
		{
			return false;
		}
		output.addInput ( this );
		return true;
	}

	/**
	 * This is the deafult implementation that works with {@link #inputs}. You should not need to touch it.
	 */
	public boolean removeInput ( Node input )
	{
		if ( inputs == null )
		{
			getInputs ();
		}
		if ( !inputs.remove ( input ) )
		{
			return false;
		}
		input.removeOutput ( this );
		return true;
	}

	/**
	 * This is the deafult implementation that works with {@link #inputs}. You should not need to touch it.
	 */
	public boolean removeOutput ( Node output )
	{
		if ( outputs == null )
		{
			getOutputs ();
		}
		if ( !outputs.remove ( output ) )
		{
			return false;
		}
		output.removeInput ( this );
		return true;
	}

	/**
	 * Compares to another node based on the first header/value pair returned by {@link #getTabValues()}. Note that when
	 * this returns the same cell value/type, it further checks if o == this, only in that case it returns 0, ie two nodes
	 * are equivalent only if they're the same. This is needed by the way {@link ChainsBuilder} works.
	 */
	public int compareTo ( Node o )
	{
		if ( o == null )
		{
			return -1;
		}
		if ( this == o )
		{
			return 0;
		}

		if ( ! ( o instanceof DefaultAbstractNode ) )
		{
			throw new IllegalArgumentException ( "Cannot compare DefaultAbstractNode with" + o.getClass ().getSimpleName () );
		}

		String label = null;
		List<TabValueGroup> tbvs = getTabValues ();
		if ( !tbvs.isEmpty () )
		{
			label = tbvs.get ( 0 ).getValues ().get ( 0 );
		}

		String olabel = null;
		List<TabValueGroup> otbvs = o.getTabValues ();
		if ( !otbvs.isEmpty () )
		{
			olabel = otbvs.get ( 0 ).getValues ().get ( 0 );
		}

		if ( label != null )
		{
			if ( olabel == null )
			// null labels always before non-nulls
			{
				return 1;
			}

			int diff = label.compareToIgnoreCase ( olabel );

			if ( diff != 0 )
			{
				return diff;
			}
			// We already ruled out 0 (when they're the same)
			return this.id - ( (DefaultAbstractNode) o ).id > 0 ? 1 : -1;
		} else if ( olabel == null )
		// We already ruled out 0 (when they're the same)
		{
			return this.id - ( (DefaultAbstractNode) o ).id > 0 ? 1 : -1;
		} else
		// null labels always before non-nulls
		{
			return -1;
		}
	}
	
	/**
	 * The default is getTabValues ().get ( 0 ).getHeaders ().get ( 0 ), i.e.: the first header, 
	 * something like 'Source Name' or 'Protocol REF'. There might be cases where the type is a different string, 
	 * e.g.: there might be two nodes both having 'Protocol REF' as first header but having the types 
	 * 'Sampling Protocol' and 'Extraction Protocol'.
	 * 
	 */
	public String getType ()
	{
		return this.getTabValues ().get ( 0 ).getHeaders ().get ( 0 );
	}

	/**
	 * Default is always -1, ie, there is no particular restriction over the node order in an experimental work-flow.
	 * This is fine if you don't expect uneven graphs, ie, graphs where all the paths from sources (left) to sinks (right)
	 * have the same length (which also means the corresponding table has no gap). If that is not the case, you will need
	 * to override this method and define something for it.
	 *   
	 */
	public int getOrder ()
	{
		return -1;
	}

	/**
	 * Two nodes are equivalent only if o == this. This is so because {@link ChainsBuilder} duplicate nodes in order to
	 * get a graph of chains that correspond to the rows of the final exported spreadsheet.
	 */
	@Override
	public boolean equals ( Object o )
	{
		return this == o;
	}

	/**
	 * Two nodes are equivalent only if o == this. This is so because {@link ChainsBuilder} duplicate nodes in order to
	 * get a graph of chains that correspond to the rows of the final exported spreadsheet. This class has an internal
	 * static integer identifier, which is used for the hash code and in {@link #compareTo(Node)}.
	 */
	@Override
	public int hashCode ()
	{
		return (int) id;
	}

	/**
	 * returns the first header/value pair returned by {@link #getTabValues()}. This should be useful for debugging.
	 */
	@Override
	public String toString ()
	{
		List<TabValueGroup> tbvs = getTabValues ();
		if ( tbvs.isEmpty () )
		{
			return "{Node " + id + "}";
		}
		TabValueGroup tbg = tbvs.get ( 0 );
		return tbg.getHeaders ().get ( 0 ) + ": " + tbg.getValues ().get ( 0 );
	}

}
