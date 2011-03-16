package org.isatools.tablib.export.graph_algorithm.layering_tests;

import static java.lang.System.out;

import java.util.HashSet;
import java.util.Set;

import org.isatools.tablib.export.graph_algorithm.LayersBuilder;
import org.isatools.tablib.export.graph_algorithm.TableBuilder;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioExtract;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioSample;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioSource;
import org.isatools.tablib.export.graph_algorithm.layering_tests.wrappers.LayeringModelTableBuilder;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.Data;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ExperimentNode;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ProtocolRef;
import org.junit.Test;

/**
 * Tests for cases of uneven graphs, which use the {@link LayersBuilder}. 
 *  
 * TODO: results are inspected manually, we need to write validation. 
 * 
 * <dl><dt>date</dt><dd>Mar 2, 2011</dd></dl>
 * @author brandizi
 *
 */
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
		sample.addCharacteristic ( "Material Type", "RNA", "RNA", "MGED-Ontology" );

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
	
	/**
	 * <p>Tests the graph:<br/><br/> <img src = 'ex_uneven_graph_1.gif' />.</p>
	 *  
	 * <p>Which yelds this:</p>
	 * <p> 
	 * <pre>
	 *  Source Name |                    Sample Name |                    Sample Name |                   Protocol REF |                   Protocol REF |                   Extract Name | 
	 *         null |                       Sample 1 |                       Sample 2 |                           null |                           null |                             x1 | 
	 *         null |                       Sample 1 |                           null |            treating protocol 1 |            treating protocol 2 |                             x1 | 
	 *     source 1 |                           null |                       Sample 2 |                           null |                           null |                             x1 | 
	 *     source 1 |                       Sample 3 |                           null |            sampling protocol 3 |                           null |                           null | 
	 *     source 1 |                           null |                           null |                           null |                           null |                             x2 | 	 
	 * </pre>
	 * </p>
	 */
	@Test
	public void testUnevenGraph1 ()
	{
		out.println ( "_______ TEST UNEVEN GRAPH 1 ________ " );

		BioSource sr1 = new BioSource ( "source 1" );
		sr1.addCharacteristic ( "Organism", "Mus Musculus", "123", "NCBI" );
		sr1.addCharacteristic ( "Age", "10 weeks", null, null );

		BioSample sm1 = new BioSample ( "Sample 1" );
		sm1.addCharacteristic ( "Material Type", "RNA", "RNA", "MGED-Ontology" );
		
		BioSample sm3 = new BioSample ( "Sample 3" );

		sm3.addInput ( sr1 );
		
		
		ProtocolRef p3 = new ProtocolRef ( "sampling protocol 3" );
		p3.addParameter ( "Sampling Quantity", "10 ml", null, null );

		p3.addInput ( sm3 );
		
		BioSample sm2 = new BioSample ( "Sample 2" );
		sm2.addCharacteristic ( "Organism Part", "liver", null, null );
		
		sm2.addInput ( sm1 );
		sm2.addInput ( sr1 );
		
		ProtocolRef p1 = new ProtocolRef ( "treating protocol 1" );

		p1.addInput ( sm1 );
		
		ProtocolRef p2 = new ProtocolRef ( "treating protocol 2" );
		
		p2.addInput ( p1 );
		
		BioExtract x1 = new BioExtract ( "x1" );

		BioExtract x2 = new BioExtract ( "x2" );
		x2.addCharacteristic ( "Material Type", "DNA", "123", "OBI" );

		x1.addInput ( p2 );
		x1.addInput ( sm2 );
		x2.addInput ( sr1 );

		Set<ExperimentNode> nodes = new HashSet<ExperimentNode> ();
		nodes.add ( x1 );
		nodes.add ( x2 );
		nodes.add ( p3 );

		TableBuilder tb = new LayeringModelTableBuilder ( nodes );
		out.println ( tb.report () );
	}
	
	@Test
	public void testCloseSameTypes ()
	{
		out.println ( "_______ TEST FOR CLOSE SAME TYPES ________ " );

		BioSample sample1 = new BioSample ( "sample 1" );
		sample1.addCharacteristic ( "Material Type", "RNA", "RNA", "MGED-Ontology" );

		BioSample sample2 = new BioSample ( "sample 2" );
		sample2.addInput ( sample1 );
		
		ProtocolRef proto1 = new ProtocolRef ( "treatment protocol 1" );
		proto1.addParameter ( "Foo Quantity", "10 ml", null, null );
		proto1.addInput ( sample1 );
		
		BioSample sample4 = new BioSample ( "sample 4" );
		sample4.addInput ( proto1 );
		
		BioExtract xtract1 = new BioExtract ( "extract 1" );
		xtract1.addInput ( sample2 );

		Set<ExperimentNode> nodes = new HashSet<ExperimentNode> ();
		nodes.add ( sample4 );
		nodes.add ( xtract1 );

		TableBuilder tb = new LayeringModelTableBuilder ( nodes );
		out.println ( tb.report () );
	}
	
}
