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
 * - Manon Delahaye (ISA team trainee: BII web services)
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
 * To request enhancements: Êhttp://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * Reciprocal Public License 1.5 (RPL1.5)
 * [OSI Approved License]
 *
 * Reciprocal Public License (RPL)
 * Version 1.5, July 15, 2007
 * Copyright (C) 2001-2007
 * Technical Pursuit Inc.,
 * All Rights Reserved.
 *
 * http://www.opensource.org/licenses/rpl1.5.txt
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab.mapping.studyFile;

import org.isatools.isatab.mapping.StudyWrapper;
import org.isatools.tablib.mapping.FormatSetTabMapper;
import org.isatools.tablib.mapping.testModels.isatab.ISATABWithStudyPipelineMapper;
import org.isatools.tablib.parser.TabLoader;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.MaterialProcessing;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.term.ProtocolType;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;
import static org.junit.Assert.*;

public class SourceProcessingTabMapperTest {


	@Test
	public void testBasics() throws Exception {
		// Load the test case, as usually
		//
		out.println("\n\n_____ Testing SamplesTabMapper, source->sample, format mapper _______");
		InputStream inputStream = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/isatab/samples/study_samples.xml"));
		FormatSet schema = SchemaBuilder.loadFormatSetFromXML(inputStream);
		assertNotNull("I didn't get a schema", schema);

		TabLoader loader = new TabLoader(schema);
		FormatInstance formatInstance = loader.parse(
				null,
				new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/test-data/isatab/samples/study_samples.csv"))),
				"study_samples"
		);
		String fileid = "study_samples.csv";
		formatInstance.setFileId(fileid);

		SectionInstance sectionInstance = formatInstance.getSectionInstance("study_samples");
		List<Record> records = sectionInstance.getRecords();
		assertNotNull("I couldn't get records!", records);
		assertTrue("I couldn't get records (empty result)!", records.size() > 0);


		// Prepare some objects referred by the test.
		// 
		Study study = new Study("A stupid test study...");
		study.setAcc("bii:test:study:foo");
		StudyWrapper studyw = new StudyWrapper(study, fileid);
		BIIObjectStore store = new BIIObjectStore();
		Protocol protocol = new Protocol(
				"P-BMAP-8",
				new ProtocolType("test.protocol.type", "Test Protocol Type", new ReferenceSource("bii:tests:protocols"))
		);
		store.put(Protocol.class, study.getAcc() + "\\P-BMAP-8", protocol);
		store.put(StudyWrapper.class, fileid, studyw);

		// Invoke the mapper
		//
		StudySamplesProcessingTabMapper mapper = new StudySamplesProcessingTabMapper(store, sectionInstance);
		mapper.map(0);

		// TODO: review the key
		MaterialProcessing processing =
				(MaterialProcessing) store.get(Processing.class, "bii:test:study:foo:proc:study_samples.0.2.0");
		assertNotNull("Couldn't get any mapped object!", processing);

		Collection<MaterialNode> inputs = processing.getInputNodes();
		assertNotNull("No inputs in result!", inputs);
		assertTrue("No inputs in result (empty collection)", inputs.size() > 0);
		MaterialNode input = inputs.iterator().next();
		Material source = input.getMaterial();
		assertNotNull("Null source!", source);
		assertEquals("Wrong Source!", "Study1.animal1", source.getName());


		Collection<MaterialNode> outputs = processing.getOutputNodes();
		assertNotNull("No outputs in result!", outputs);
		assertTrue("No outputs in result (empty collection)", outputs.size() > 0);
		MaterialNode output = outputs.iterator().next();
		Material sample = output.getMaterial();
		assertNotNull("Null sample!", sample);
		assertEquals("Wrong Sample!", "Study1.animal1.liver", sample.getName());

		out.println("\n___ RESULTS: ___");
		out.println("Processing: " + processing.toStringVerbose());
		out.println("Input: " + input);
		out.println("  Input Material: " + source);
		out.println("Output: " + output);
		out.println("  Output Material: " + sample);

		//out.println ( store.toStringVerbose () );
		out.println("\n_____ /end: Testing SamplesTabMapper, source->sample, format mapper _______\n\n");
	}


	@Test
	public void testWithFormatSetMapper() throws Exception {
		// Load the test case, as usually
		//
		out.println("\n\n____ Testing SamplesTabMapper, source->sample, protocol ____");

		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream(
				"/test-data/isatab/samples/refs_study_samples.xml"
		));
		FormatSet schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		TabLoader loader = new TabLoader(schema);

		Map<String, String> loadingMap = new HashMap<String, String>();
		loadingMap.put("/test-data/isatab/isatab_3_refs.csv", "references");
		loadingMap.put("/test-data/isatab/samples/study_samples_with_protocols.csv", "study_samples");
		loader.loadResources(loadingMap);

		BIIObjectStore store = new BIIObjectStore();
		FormatSetInstance formatSetInstance = loader.getFormatSetInstance();
		FormatSetTabMapper mapper = new ISATABWithStudyPipelineMapper(store, formatSetInstance);
		mapper.map();

		out.println("\n___ RESULTS: ___");
		out.println(store.toStringVerbose());

		out.println("\n____ /end: Testing SamplesTabMapper, source->sample, protocol ____\n");
	}


}