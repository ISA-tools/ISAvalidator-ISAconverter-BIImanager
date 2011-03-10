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

package org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests;

import org.isatools.tablib.export.graph_algorithm.TableBuilder;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.BioMaterial;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.Data;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ExperimentNode;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ProtocolRef;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.SimpleModelTableBuilder;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static java.lang.System.out;

/**
 * <dl><dt>date</dt><dd>Jun 1, 2010</dd></dl>
 *
 * @author brandizi
 */
public class SimpleBioModelTest {
	/**
	 * Tests this example, implemented as an instance of the sample bio-model:
	 * <p/>
	 * <pre>
	 * src1   proto1   sample   proto2   data1
	 * src2   proto1   sample   proto3   data2
	 * </pre>
	 */
	@Test
	public void testPipeline1() {
		out.println("_______ TEST SIMPLE MODEL 1 ________ ");

		BioMaterial src1 = new BioMaterial("source 1");
		src1.addCharacteristic("Organism", "Mus Musculus", "123", "NCBI");
		src1.addCharacteristic("Age", "10 weeks", null, null);

		BioMaterial src2 = new BioMaterial("source 2");
		src2.addCharacteristic("Organism", "Mus Musculus", "123", "NCBI");
		src2.addCharacteristic("Age", "20 weeks", null, null);

		ProtocolRef proto1 = new ProtocolRef("sampling protocol 1");
		proto1.addParameter("Sampling Quantity", "10 ml", null, null);

		src1.addOutput(proto1);
		src2.addOutput(proto1);

		BioMaterial sample = new BioMaterial("Sample");
		sample.addCharacteristic("Matrial Type", "RNA", "RNA", "MGED-Ontology");

		sample.addInput(proto1);

		ProtocolRef proto2 = new ProtocolRef("scanning protocol 2");
		proto2.addInput(sample);

		ProtocolRef proto3 = new ProtocolRef("scanning protocol 3");
		proto3.addInput(sample);

		Data data1 = new Data("file1.txt");
		data1.addAnnotation("Image Correction Method", "intensity average", "123", "OBI");
		data1.addInput(proto2);

		Data data2 = new Data("file2.txt");
		data2.addAnnotation("Image Amplification", "10x", null, null);
		data2.addInput(proto3);

		Set<ExperimentNode> nodes = new HashSet<ExperimentNode>();
		nodes.add(sample);

		TableBuilder tb = new SimpleModelTableBuilder(nodes);
		out.println(tb.report());
	}
}
