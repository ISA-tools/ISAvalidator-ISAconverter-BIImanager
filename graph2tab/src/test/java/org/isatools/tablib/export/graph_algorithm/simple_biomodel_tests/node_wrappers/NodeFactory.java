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
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. 
 * To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers;

import org.isatools.tablib.export.graph_algorithm.AbstractNodeFactory;
import org.isatools.tablib.export.graph_algorithm.DefaultAbstractNode;
import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.BioMaterial;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.Data;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ExperimentNode;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ProtocolRef;

/**
 * The custom {@link NodeFactory} for the sample BioModel.
 * <p/>
 * <dl><dt>date</dt><dd>Jun 1, 2010</dd></dl>
 *
 * @author brandizi
 */
public class NodeFactory extends AbstractNodeFactory<ExpNodeWrapper, ExperimentNode> {
	protected NodeFactory() {
	}

	private static final NodeFactory instance = new NodeFactory();

	public static NodeFactory getInstance() {
		return instance;
	}

	/**
	 * Simply, determines the right {@link ExpNodeWrapper} for each type of {@link ExperimentNode}.
	 * This is the typical implementation for your to-be-exported object-model. A wrapper factory is
	 * preferred to direct extension of {@link Node} or {@link DefaultAbstractNode}, because the
	 * graph2tab library is able to make {@link Node} duplicates, even when they back nodes from an
	 * object model that are equivalent according to equals()/hashCode().
	 */
	@Override
	protected ExpNodeWrapper createNewNode(ExperimentNode base) {
		if (base instanceof BioMaterial) {
			return new BioMaterialWrapper((BioMaterial) base, this );
		}
		if (base instanceof Data) {
			return new DataWrapper((Data) base, this );
		}
		if (base instanceof ProtocolRef) {
			return new ProtocolRefWrapper((ProtocolRef) base, this );
		}
		throw new IllegalArgumentException(
				"Node of type " + base.getClass().getSimpleName() + " not supported"
		);
	}
}
