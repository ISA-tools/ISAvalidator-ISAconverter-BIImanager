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

package org.isatools.isatab.export.isatab.pipeline.wrapper_nodes;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.export.TabExportUtils;
import org.isatools.tablib.export.TabExportUtils.OEString;
import org.isatools.tablib.export.graph_algorithm.DefaultAbstractNode;
import org.isatools.tablib.export.graph_algorithm.DefaultTabValueGroup;
import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TabValueGroup;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Annotatable;
import uk.ac.ebi.bioinvindex.model.Annotation;
import uk.ac.ebi.bioinvindex.model.processing.GraphElement;
import uk.ac.ebi.bioinvindex.model.term.*;
import uk.ac.ebi.utils.string.StringSearchUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * The {@link Node} implementation of {@link Node}, that allows to export a BII-based study to ISATAB spreadsheets
 * (sample or assay file).
 * 
 * @author brandizi <b>date</b>: Dec 13, 2009
 */
public abstract class GraphElementWrapperNode extends DefaultAbstractNode
{
	protected final BIIObjectStore store;
	protected NodeType nodeType = NodeType.REGULAR;
	protected final String assayFileId;

	protected List<TabValueGroup> tabValues = new ArrayList<TabValueGroup> ();

	protected static final Logger log = Logger.getLogger ( GraphElementWrapperNode.class );

	/**
	 * Identifies which kind of node this is.
	 */
	protected enum NodeType
	{
		/**
		 * A regular node, created by {@link WrapperNodesFactory} as a wrapper of some {@link GraphElement}
		 */
		REGULAR,

		/**
		 * The node was duplicated from a regular node by the table builder algorithm, using
		 * {@link Node#createIsolatedClone()}. We need to recognise this case, so that {@link Node#getInputs()} and
		 * {@link Node#getOutputs()} don't generate nodes from the underline BII pipeline, but only those that are
		 * explicitly attached by the table building procedure.
		 */
		CLONE,

		/**
		 * Using the assayFileId and the annotations in the BII model, the node was recognized as a start node
		 */
		START,

		/**
		 * Using the assayFileId and the annotations in the BII model, the node was recognized as an end node
		 */
		END
	}

	/**
	 * The store is needed for populating it with ontology sources met over the pipeline.
	 */
	protected GraphElementWrapperNode ( BIIObjectStore store, String assayFileId )
	{
		super ();
		this.store = store;
		this.assayFileId = assayFileId;
	}

	/**
	 * This should be used by the implementation of {@link Node#createIsolatedClone()}, to create a clone from the
	 * original parameter.
	 */
	protected GraphElementWrapperNode ( GraphElementWrapperNode original )
	{
		this ( original.store, original.assayFileId );
		this.nodeType = NodeType.CLONE;
		this.tabValues = original.tabValues;
		this.inputs = new TreeSet<Node> ();
		this.outputs = new TreeSet<Node> ();
	}

	/**
	 * Adds an ontology term to the headers/values for this element, the postfix is appended to the header and removed
	 * later, this is needed to avoid to mix the same headers about distinct elements
	 */
	protected void initOntoTerm ( DefaultTabValueGroup tabValueGroup, OntologyTerm ot )
	{
		OEString otstr = TabExportUtils.storeSource ( store, ot );

		tabValueGroup.add ( "Term Source REF", otstr.src );
		tabValueGroup.add ( "Term Accession Number", otstr.acc );
	}

	/**
	 * Helper that adds a property to the headers/values.
	 */
	protected void initProperty ( String header, String value, OntologyTerm ot, UnitValue uv )
	{
		DefaultTabValueGroup tbg = new DefaultTabValueGroup ( header, value );

		// Add the unit
		// Unit may be present only if no OT is defined
		if ( uv != null )
		{
			// TODO: Currently the ISATAB doesn't support the reporting of the Unit type
			tbg.add ( "Unit", uv.getValue () );
			initOntoTerm ( tbg, uv.getSingleOntologyTerm () );
		} else
		// If it's not a value with unit, it may have onto terms
		{
			initOntoTerm ( tbg, ot );
		}

		tabValues.add ( tbg );
	}

	/**
	 * Helper that adds a FV to the headers/values
	 */
	protected void initFactorValue ( FactorValue fv )
	{
		Factor type = fv.getType ();
		initProperty ( "Factor Value [" + type.getValue () + "]", fv.getValue (), fv.getSingleOntologyTerm (), fv.getUnit () );
	}

	/**
	 * Adds all the annotations of a certain type, rendering them with the specified header.
	 */
	protected void initAnnotations ( Annotatable source, String annType, String header )
	{
		for ( String annv: source.getAnnotationValues ( annType ) )
		{
			tabValues.add ( new DefaultTabValueGroup ( header, StringUtils.trimToNull ( annv ) ) );
		}
	}

	/**
	 * A wrapper that calls {@link #initAnnotations(Annotatable, String, String)} for every pair of annotation type /
	 * header in the parameter list.
	 * 
	 * @param annTypeAndHeaders
	 *          a list in the format &lt;annType, header&gt;+ (ie: at least one pair needed).
	 */
	protected void initAnnotations ( Annotatable source, String ... annTypeAndHeaders )
	{
		if ( annTypeAndHeaders.length < 2 )
		{
			throw new TabInternalErrorException ( "I need at least one pair of annotation-type and header" );
		}

		for ( int i = 0; i < annTypeAndHeaders.length; i++ )
		{
			initAnnotations ( source, annTypeAndHeaders[i], annTypeAndHeaders[++i] );
		}
	}

	/**
	 * Adds all the comment annotations to this element's headers/values
	 */
	protected void initComments ( Annotatable source )
	{
		for ( Annotation ann: source.getAnnotationValuesByRe ( AnnotationType.COMMENT_RE ) )
		{
			AnnotationType atype = ann.getType ();
			String ctype = atype.getValue ().substring ( AnnotationType.COMMENT_PREFX_LEN );
			tabValues.add ( new DefaultTabValueGroup ( "Comment [" + ctype + "]", ann.getText () ) );
		}
	}

	/**
	 * Tells if the string contains one of the matches. This is often used to detect which type of material/data one has.
	 * TODO: Was moved to {@link StringSearchUtils}, replace it.
	 */
	@Deprecated
	protected static boolean containsOne ( String target, String ... matches )
	{
		if ( target == null )
		{
			throw new TabInternalErrorException ( "containsOne(): target is null!" );
		}
		if ( matches == null || matches.length == 0 )
		{
			throw new TabInternalErrorException ( "containsOne(): target is null!" );
		}
		for ( String match: matches )
		{
			if ( StringUtils.containsIgnoreCase ( target, match ) )
			{
				return true;
			}
		}
		return false;
	}

	public List<TabValueGroup> getTabValues ()
	{
		return tabValues;
	}

}
