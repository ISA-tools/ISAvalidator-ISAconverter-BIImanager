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

package org.isatools.isatab_v1;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.isatools.isatab_v1.mapping.ISATABMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.*;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.term.Design;
import uk.ac.ebi.bioinvindex.model.term.FactorValue;
import uk.ac.ebi.bioinvindex.model.term.PropertyValue;
import uk.ac.ebi.bioinvindex.model.term.ProtocolType;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.System.out;
import static org.junit.Assert.*;


public class ISATABMappingIntegrationTest {
    /**
     * Tests that a (almost) complete ISATAB is correctly loaded and mapped into BII. Additionally produces graphs of the
     * mapped experimental pipelines. The latter may be visualize by means of GraphViz, and it's stored in
     * target/
     */
    @Test
    @SuppressWarnings("static-access")
    public void testLoadingAndMapping() throws Exception {
        out.println("\n\n__________ ISATAB Mapping Test __________\n\n");

        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1";

        ISATABLoader loader = new ISATABLoader(filesPath);
        FormatSetInstance isatabInstance = loader.load();

        BIIObjectStore store = new BIIObjectStore();
        ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

        isatabMapper.map();
        assertTrue("Oh no! No mapped object! ", store.size() > 0);

        Study study1 = store.getType(Study.class, "BII-S-1");
        assertNotNull("Oh no! Study BII-S-2 not found in the results!", study1);

        Collection<Design> designs = study1.getDesigns();
        assertNotNull("Oh no! Study has no designs!", designs);
        assertEquals("Oh no! Study has bad no. of designs!", 1, designs.size());

        Study study2 = store.getType(Study.class, "BII-S-2");
        assertNotNull("Oh no! Study BII-S-1 not found in the results!", study2);

        Collection<Protocol> protos = study2.getProtocols();
        assertNotNull("Oh no! No protocol mapped to the study!", protos);
        assertEquals("Oh no! Wrong no. of protocols mapped to the study!", 5, protos.size());

        Collection<AssayResult> ars = study1.getAssayResults();
        assertEquals("Oh no! Wrong no. of AssayResult(s)", 175, ars.size());

        Collection<AssayResult> ars2 = study2.getAssayResults();
        assertEquals("Oh no! Wrong no. of AssayResult(s)", 14, ars2.size());

        out.println("\n\n__ RESULTS: __  ");
        out.println(store.toStringVerbose());

        Collection<Identifiable> objects = new ArrayList<Identifiable>();
        objects.addAll(store.values(Processing.class));
        DotGraphGenerator dotter = new DotGraphGenerator(objects);
        String dotFileName = baseDir + "/target/isatab.dot";
        dotter.createGraph(dotFileName);
        out.println("\n\nExperimental Graph written in " + dotFileName);

        out.println("\n\n__________ /end: ISATAB Mapping Test __________\n\n\n\n");
    }


    /**
     * Test a case with transcriptomics only. Again, mapping and graph produced.
     */
    @Test
    @Ignore
    // TODO
    public void testSimpleISATAB() throws Exception {
        out.println("\n\n__________ ISATAB Mapping Graph Test __________\n\n");

        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab/example_tx";
        ISATABLoader loader = new ISATABLoader(filesPath);
        FormatSetInstance isatabInstance = loader.load();

        BIIObjectStore store = new BIIObjectStore();
        ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

        isatabMapper.map();
        assertTrue("Oh no! No mapped object! ", store.size() > 0);

        out.println("__ RESULTS: __  ");
        out.println(store.toStringVerbose());

        Collection<Identifiable> objects = new ArrayList<Identifiable>();
        objects.addAll(store.values(Processing.class));
        DotGraphGenerator dotter = new DotGraphGenerator(objects);
        String dotFileName = baseDir + "/target/isatab_tx.dot";
        dotter.createGraph(dotFileName);
        out.println("\n\nExperimental Graph written in " + dotFileName);

        out.println("\n\n__________ /end: ISATAB Mapping Graph Test __________\n\n\n\n");
    }


    /**
     * Tests that factor values in the study file are correctly reported into the corresponding assay results
     */
    @Test
    @Ignore
    // TODO?
    @SuppressWarnings("static-access")
    public void testFactorsInStudyFile() throws Exception {
        out.println("\n\n__________ ISATAB Mapping Test __________\n\n");

        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab_v1rc1/griffin_gauguier_sample_fv";
        ISATABLoader loader = new ISATABLoader(filesPath);
        FormatSetInstance isatabInstance = loader.load();

        BIIObjectStore store = new BIIObjectStore();
        ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

        isatabMapper.map();
        assertTrue("Oh no! No mapped object! ", store.size() > 0);


        Study study = store.getType(Study.class, "bii:study:1");
        assertNotNull("Oh no! Study bii:study:1 not found in the results!", study);

        Collection<Design> designs = study.getDesigns();
        assertNotNull("Oh no! Study has no designs!", designs);
        assertEquals("Oh no! Study has bad no. of designs!", 2, designs.size());

        Collection<Protocol> protos = study.getProtocols();
        assertNotNull("Oh no! No protocol mapped to the study!", protos);
        assertEquals("Oh no! Wrong no. of protocols mapped to the study!", 16, protos.size());

        final String pname = "standard procedure 1";
        Protocol proto = (Protocol) CollectionUtils.find(protos, new Predicate() {
            public boolean evaluate(Object proto) {
                return pname.equals(((Protocol) proto).getName());
            }
        });
        assertNotNull("Argh! Protocol '" + pname + "' not found!", proto);
        assertEquals("Ops! Wrong version for the protocol '" + pname + "'", "v1.0", proto.getVersion());

        ProtocolType ptype = proto.getType();
        assertNotNull("Urp! Protocol type for '" + pname + "' should not be null!", ptype);
        assertEquals("Argh! Wrong protocol type for '" + pname + "'!", "animal procedure", ptype.getName());
        ReferenceSource ptypeSrc = ptype.getSource();
        assertNotNull("Argh No protocol type source for '" + pname + "' / " + ptype + " !", ptypeSrc);
        assertEquals("Argh! Protocol type source is wrong for '" + pname + "' / " + ptype + " !", "OBI", ptypeSrc.getAcc());


        Collection<Assay> assays = study.getAssays();
        Assay assay = (Assay) CollectionUtils.find(assays, new Predicate() {
            public boolean evaluate(Object assay) {
                return StringUtils.trimToEmpty(((Assay) assay).getAcc()).startsWith(
                        "bii:study:1:assay:Study1.animal6.liver.extract1.le1.hyb1");
            }
        });
        assertNotNull("Gulp! Assay ...le1.hyb1.11 not found!", assay);

        Collection<AssayResult> ars = study.getAssayResults();
        out.println("__ ASSAY RESULTS: __  ");
        for (AssayResult ar : ars) {
            out.println(ar);
        }
        assertEquals("Oh no! Wrong no. of AssayResult(s)", 24, ars.size());

        ars = ProcessingUtils.findAssayResultsFromAssay(assay);
        assertEquals("Urp! Wrong no. of AssayResult(s) for assay test case", 1, ars.size());
        AssayResult ar = ars.iterator().next();

        out.println("__ CASCADED VALUES ON THE TEST CASE: __  ");
        Collection<PropertyValue> arProps = ar.filterRepeatedPropertyValues(ar.getCascadedPropertyValues());
        boolean myFactorFound = false;
        for (PropertyValue<?> v : arProps) {
            out.println("    " + v);

            if (!(v instanceof FactorValue)) {
                continue;
            }
            FactorValue fv = (FactorValue) v;
            if (!"Operator".equals(fv.getType().getValue())) {
                continue;
            }
            assertFalse("Ouch! The factor value " + fv + " appears twice in the assay-result test case", myFactorFound);
            myFactorFound = true;
        }
        assertEquals("Urp! Wrong number of cascaded properties returned by mapped assay!", 12, arProps.size());
        assertTrue("Ouch! Factor value 'Operator' not found in the assay-result test case", myFactorFound);

        out.println("\n\n__ RESULTS: __  ");
        out.println(store.toStringVerbose());

        Collection<Identifiable> objects = new ArrayList<Identifiable>();
        objects.addAll(store.values(Processing.class));
        DotGraphGenerator dotter = new DotGraphGenerator(objects);
        String dotFileName = baseDir + "/target/isatab_sample_fv.dot";
        dotter.createGraph(dotFileName);
        out.println("\n\nExperimental Graph written in " + dotFileName);

        out.println("\n\n__________ /end: ISATAB Mapping Test __________\n\n\n\n");
    }

    @Test
    public void testCommentsInInvestigationFile() throws Exception {
        out.println("\n\n__________ testCommentsInInvestigationFile __________\n\n");

        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1-with-comments";
        ISATABLoader loader = new ISATABLoader(filesPath);
        FormatSetInstance isatabInstance = loader.load();

        BIIObjectStore store = new BIIObjectStore();
        ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

        isatabMapper.map();
        assertTrue("Oh no! No mapped object! ", store.size() > 0);

        Study study = store.getType(Study.class, "BII-S-1");
        assertNotNull("Oh no! Study bii:study:1 not found in the results!", study);

        int commentCount = 0;
        for (Investigation investigation : study.getInvestigations()) {
            Collection<Annotation> annotations = investigation.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.getType().getValue().contains("comment")) {
                    commentCount++;
                }
            }
            assertEquals("Not as many comments in the investigation section " + investigation.getAcc() + " as I was expecting", 5, commentCount);

            commentCount = 0;
            for (Publication publication : investigation.getPublications()) {
                for (Annotation annotation : publication.getAnnotations()) {
                    if (annotation.getType().getValue().contains("comment")) {
                        commentCount++;
                    }
                }
                assertEquals("Not as many comments in the investigation publication section as I was expecting", 0, commentCount);
            }
        }

        for (Contact contact : study.getContacts()) {
            commentCount = 0;
            for (Annotation annotation : contact.getAnnotations()) {
                if (annotation.getType().getValue().contains("comment")) {
                    commentCount++;
                }
            }
            assertEquals("Not as many comments in study contacts for BII-S-1 as I was expecting", 1, commentCount);
        }

        for (Publication publication : study.getPublications()) {
            commentCount = 0;
            for (Annotation annotation : publication.getAnnotations()) {
                if (annotation.getType().getValue().contains("comment")) {
                    commentCount++;
                }
            }
            assertEquals("Not as many comments in study publications for BII-S-1 as I was expecting", 1, commentCount);
        }

        commentCount = 0;
        for (Protocol protocol : study.getProtocols()) {
            for (Annotation annotation : protocol.getAnnotations()) {
                if (annotation.getType().getValue().contains("comment")) {
                    commentCount++;
                }
            }

        }
        assertEquals("Not as many comments in study protocols for BII-S-1 as I was expecting", 3, commentCount);
    }

    @Test
    /**
     * Tests how the code performs when receiving 'ontologised' characteristics, factor values, etc.
     */
    public void testOntologisedExtendedFields() throws Exception {
        out.println("\n\n__________ testOntologisedPropertyExtension __________\n\n");

        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1-with-ontology-extensions";
        ISATABLoader loader = new ISATABLoader(filesPath);
        FormatSetInstance isatabInstance = loader.load();

        BIIObjectStore store = new BIIObjectStore();
        ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

        isatabMapper.map();
        assertTrue("Oh no! No mapped object! ", store.size() > 0);

        Study study = store.getType(Study.class, "BII-S-1");
        assertNotNull("Oh no! Study bii:study:1 not found in the results!", study);

        // todo add tests to ensure that we have the expected values in place.
    }

}
