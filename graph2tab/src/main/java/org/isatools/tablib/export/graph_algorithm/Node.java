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

import java.util.List;
import java.util.SortedSet;

/**
 * The Node interface.
 * <p/>
 * Defines the minimal that is needed for what concerns the conversion of an experimental graph into a tabular
 * representation (such as ISATAB or MAGETAB).
 * <p/>
 * What it is needed is essentially: 1) the set of input and output nodes and 2) the list of header/value pairs that
 * will represent the graph node in the final spreadsheet. For instance, a biological sample object would be a node and
 * &lt;Characteristic [ "Organism" ],mus musculus&gt; would be an example of element provided by {@link #getTabValues()}.
 * <p/>
 * <dl>
 * <dt>date</dt>
 * <dd>May 10, 2010</dd>
 * </dl>
 * 
 * @author brandizi
 */
public interface Node extends Comparable<Node>
{
	/**
	 * The inputs of the node. Should not return a modifiable set.
	 * <p/>
	 * <p>
	 * Nodes are sorted, because we need to rely on the same order over multiple calls of this method. The particular
	 * order you implement can be arbitrary, the important thing is that there is one. {@link DefaultAbstractNode} has a
	 * convenient implementation of {@link DefaultAbstractNode#compareTo(Node)}.
	 * </p>
	 */
	public SortedSet<Node> getInputs ();

	/**
	 * Modifiers <b>must be symmetric</b>, when an input is added/removed, the corresponding output is added removed on
	 * the other side.
	 * <p>
	 * As usually, they should return true if they made actual changes.
	 * </p>
	 */
	public boolean addInput ( Node input );

	/**
	 * Modifiers <b>must be symmetric</b>, when an input is added/removed, the corresponding output is added/removed on
	 * the other side.
	 * <p>
	 * As usually, they should return true if actual changes was actually made.
	 * </p>
	 */
	public boolean removeInput ( Node input );

	/**
	 * The outputs of the node. Should not return a modifiable set.
	 * <p/>
	 * <p>
	 * Nodes are sorted, because we need to rely on the same order over multiple calls of this method. The particular
	 * order you implement can be arbitrary, the important thing is that there is one. {@link DefaultAbstractNode} has a
	 * convenient implementation of {@link DefaultAbstractNode#compareTo(Node)}.
	 * </p>
	 */
	public SortedSet<Node> getOutputs ();

	/**
	 * Modifiers <b>must be symmetric</b>, when an input is added/removed, the corresponding output is added removed on
	 * the other side.
	 * <p>
	 * As usually, they should return true if they made actual changes.
	 * </p>
	 */
	public boolean addOutput ( Node output );

	/**
	 * Modifiers <b>must be symmetric</b>, when an input is added/removed, the corresponding output is added removed on
	 * the other side.
	 * <p>
	 * As usually, they should return true if actual changes was actually made.
	 * </p>
	 */
	public boolean removeOutput ( Node output );

	/**
	 * The pairs of header/value that this node need to report in the exported spreadsheet. Should return an unmodifiable
	 * structure.
	 */
	public List<TabValueGroup> getTabValues ();

	/**
	 * The algorithm implemented in {@link ChainsBuilder} needs to make detached duplicates of nodes, ie: nodes that
	 * represent the same experimental entity of the source and haven't any of its incident edges (which will be properly
	 * added by the export logics). This is what this method has to do. The result should return exactly the same result
	 * for {@link #getTabValues()}. It is <b>very</b> important that the result is <b>distinct</b> from to the origin, ie:
	 * {@link Object#equals(Object)} and {@link Object#hashCode()} must reflect such diversity.
	 */
	public Node createIsolatedClone ();

	/**
	 * This is information to be used for rearranging the graph layering when two nodes fall into the same layer but have 
	 * a different type (the type is always the first header returned by {@link #getTabValues()}).
	 * This can be assigned in different ways. One way is to say that eg, the order of a node of type 'Source Name' is 0, 
	 * 'Sample Name' is 1 etc. Nodes that have not a particular oder requirement, such as 'Protocol REF' should have -1
	 * as order (-1 should be the default value). Another approach, to be applied to graphs initially created from tabular
	 * input (eg, from a MAGETAB or ISATAB submission), is to track the column the nodes came from and return this as order. 
	 * We manage the layering rearrangement with this parameter independently on the particular meaning you assign to it. 
	 * For instance, an advanced thing to to in the case of 'Protocol REF' nodes could be going back to the protcol type
	 * that the node refers to and establish the order on the basis of that, eg, 'Sample Treatment' come before 
	 * 'Labeled Extract'. 
	 * 
	 * See also LayerBuilder TODO.
	 */
	public int getOrder ();

}
