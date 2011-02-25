package org.isatools.tablib.export.graph_algorithm.layering_tests;

import static java.lang.System.out;

import java.util.HashSet;
import java.util.Set;

import org.isatools.tablib.export.graph_algorithm.TableBuilder;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioSample;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioSource;
import org.isatools.tablib.export.graph_algorithm.layering_tests.wrappers.LayeringModelTableBuilder;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.BioMaterial;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.Data;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ExperimentNode;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ProtocolRef;
import org.junit.Test;

public class LayeringTest
{
	/**
	 * Tests this example, implemented as an instance of the sample bio-model:
	 * <p/>
	 * 
	 * <pre>
	 * src1   proto1   sample                      data1
	 * src2   proto1   sample   sample1   proto2   data2
	 * </pre>
	 */
	@Test
	public void testPipeline1 ()
	{
		out.println ( "_______ TEST LAYERING 1 ________ " );

		BioSource src1 = new BioSource ( "source 1" );
		src1.addCharacteristic ( "Organism", "Mus Musculus", "123", "NCBI" );
		src1.addCharacteristic ( "Age", "10 weeks", null, null );

		BioSource src2 = new BioSource ( "source 2" );
		src2.addCharacteristic ( "Organism", "Mus Musculus", "123", "NCBI" );
		src2.addCharacteristic ( "Age", "20 weeks", null, null );

		ProtocolRef proto1 = new ProtocolRef ( "sampling protocol 1" );
		proto1.addParameter ( "Sampling Quantity", "10 ml", null, null );

		src1.addOutput ( proto1 );
		src2.addOutput ( proto1 );

		BioSample sample = new BioSample ( "sample" );
		sample.addCharacteristic ( "Matrial Type", "RNA", "RNA", "MGED-Ontology" );

		sample.addInput ( proto1 );
		
		BioSample sample1 = new BioSample ( "sample 1" );
		sample1.addInput ( sample );

		ProtocolRef proto2 = new ProtocolRef ( "scanning protocol 2" );
		proto2.addInput ( sample1 );

		Data data1 = new Data ( "file1.txt" );
		data1.addAnnotation ( "Image Correction Method", "intensity average", "123", "OBI" );
		data1.addInput ( sample );

		Data data2 = new Data ( "file2.txt" );
		data2.addAnnotation ( "Image Amplification", "10x", null, null );
		data2.addInput ( proto2 );

		Set<ExperimentNode> nodes = new HashSet<ExperimentNode> ();
		nodes.add ( sample );

		TableBuilder tb = new LayeringModelTableBuilder ( nodes );
		out.println ( tb.report () );
	}
}
