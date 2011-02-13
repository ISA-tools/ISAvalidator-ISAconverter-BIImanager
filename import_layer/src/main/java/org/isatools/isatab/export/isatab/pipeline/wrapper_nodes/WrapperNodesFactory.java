/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee:  BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab.export.isatab.pipeline.wrapper_nodes;

import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.export.graph_algorithm.AbstractNodeFactory;
import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.GraphElement;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.Processing;

/**
 * The factory used to return {@link Node} wrappers of BII model objects that are subclasses of {@link GraphElement}.
 * See also {@link AbstractNodeFactory}.
 * <p/>
 * <dl><dt>date</dt><dd>May 11, 2010</dd></dl>
 *
 * @author brandizi
 */
public class WrapperNodesFactory extends AbstractNodeFactory<GraphElementWrapperNode, GraphElement> {
	private static WrapperNodesFactory instance;
	private BIIObjectStore store;
	private String assayFileId;


	private WrapperNodesFactory(BIIObjectStore store, String assayFileId) {
		super();
		this.store = store;
		this.assayFileId = assayFileId;
	}

	public static synchronized WrapperNodesFactory getInstance() {
		return instance;
	}

	/**
	 * You'll need these parameters to create wrappers.
	 */
	public static synchronized WrapperNodesFactory createInstance(BIIObjectStore store, String assayFileId) {
		instance = new WrapperNodesFactory(store, assayFileId);
		return instance;
	}

	/**
	 * This manages the creation of the wrappers for the different {@link GraphElement} objets.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Node createNewNode(GraphElement base) {
		if (base instanceof MaterialNode) {
			return new MaterialWrapperNode(store, (MaterialNode) base, assayFileId);
		}
		if (base instanceof DataNode) {
			return new DataWrapperNode(store, (DataNode) base, assayFileId);
		}
		if (base instanceof Processing) {
			return new ProcessingWrapperNode(store, (Processing) base, assayFileId);
		}
		throw new TabInternalErrorException("Cannot deal with class of type " + base.getClass().getSimpleName());
	}

}
