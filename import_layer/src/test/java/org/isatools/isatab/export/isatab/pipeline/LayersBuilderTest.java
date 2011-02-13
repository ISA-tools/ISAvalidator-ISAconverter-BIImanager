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

package org.isatools.isatab.export.isatab.pipeline;

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.exceptions.TabUnsupportedException;
import org.isatools.tablib.mapping.pipeline.ProcessingEntityTabMapper;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.Annotation;
import uk.ac.ebi.bioinvindex.model.term.AnnotationType;
import uk.ac.ebi.bioinvindex.model.term.MaterialRole;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.utils.testmodels.FullStudyPipelineModel;
import uk.ac.ebi.bioinvindex.utils.testmodels.SeparatedFilesModel;

import java.lang.reflect.Method;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LayersBuilderTest {
    private static final String FOO_ASSAY_FILE_NAME = "foo_assay_file.csv";

    @Test
    public void testComputeLayers() throws Exception {
        out.println("\n\n" + StringUtils.center(" Testing LayerBuilders.computeAllLayers() ", 120, "-") + "\n");

        FullStudyPipelineModel pip = new FullStudyPipelineModel();
        pip.as1.addAnnotation(
                new Annotation(new AnnotationType(ProcessingEntityTabMapper.ASSAY_FILE_ANNOTATION_TAG),
                        FOO_ASSAY_FILE_NAME
                ));


        Method testedMethod = LayersBuilder.class.getDeclaredMethod("computeAllLayers");
        testedMethod.setAccessible(true);

        LayersBuilder lb = new LayersBuilder(new BIIObjectStore(), pip.study, false, FOO_ASSAY_FILE_NAME);
        testedMethod.invoke(lb);

        GraphLayers gls = lb.getGraphLayers();
        out.print(gls.report());

        assertEquals("Wrong layer for src1", 0, gls.getLayer(pip.nsrc1));
        assertEquals("Wrong layer for src2", 0, gls.getLayer(pip.nsrc2));
        assertEquals("Wrong layer for p1", 1, gls.getLayer(pip.p1));
        assertEquals("Wrong layer for nas1", 2, gls.getLayer(pip.nas1));
        assertEquals("Wrong layer for p2", 3, gls.getLayer(pip.p2));
        assertEquals("Wrong layer for nar1", 4, gls.getLayer(pip.nar1));
        assertEquals("Wrong layer for nar2", 4, gls.getLayer(pip.nar2));

        out.println("\n" + StringUtils.center(" /end: Testing LayerBuilders.computeAllLayers() ", 120, "-") + "\n");

    }


    @Test
    public void testComputeTypedLayers() throws Exception {
        out.println("\n\n" + StringUtils.center(" Testing LayerBuilders.computeTypedLayers() ", 120, "-") + "\n");

        FullStudyPipelineModel pip = new FullStudyPipelineModel();
        pip.as1.addAnnotation(
                new Annotation(new AnnotationType(ProcessingEntityTabMapper.ASSAY_FILE_ANNOTATION_TAG),
                        FOO_ASSAY_FILE_NAME
                ));

        MaterialRole sampleRole = new MaterialRole(
                "bii:tests:material:sample", "Sample",
                new ReferenceSource("bii:tests:ontos:foo", "Foo Ontology")
        );
        pip.src2.setType(sampleRole);

        LayersBuilder lb = new LayersBuilder(new BIIObjectStore(), pip.study, false, FOO_ASSAY_FILE_NAME);

        boolean isFailed = false;
        try {
            GraphLayers gls = lb.getGraphLayers();
        }
        catch (TabUnsupportedException e) {
            isFailed = true;
        }

        assertTrue("typed layers are not supported yet and this test should generate an exception!", isFailed);

// TODO: restore when the behavior will be supported
//		assertEquals ( "Wrong layer for src2", 0, gls.getLayer ( pip.nsrc2 ) );
//		assertEquals ( "Wrong layer for src1", 1, gls.getLayer ( pip.nsrc1 ) );
//		assertEquals ( "Wrong layer for p1", 2, gls.getLayer ( pip.p1 ) );
//		assertEquals ( "Wrong layer for nas1", 3, gls.getLayer ( pip.nas1 ) );
//		assertEquals ( "Wrong layer for p2", 4, gls.getLayer ( pip.p2 ) );
//		assertEquals ( "Wrong layer for nar1", 5, gls.getLayer ( pip.nar1 ) );
//		assertEquals ( "Wrong layer for nar2", 5, gls.getLayer ( pip.nar2 ) );

        out.println("\n" + StringUtils.center(" /end: Testing LayerBuilders.computeTypedLayers() ", 120, "-") + "\n");
    }


    @Test
    public void testSeparatedAssayFile() {
        out.println("\n\n" + StringUtils.center(" Testing layering of assay file only ", 120, "-") + "\n");

        SeparatedFilesModel pip = new SeparatedFilesModel();
        pip.as1.addAnnotation(
                new Annotation(new AnnotationType(ProcessingEntityTabMapper.ASSAY_FILE_ANNOTATION_TAG),
                        FOO_ASSAY_FILE_NAME
                ));

        LayersBuilder lb = new LayersBuilder(new BIIObjectStore(), pip.study, false, FOO_ASSAY_FILE_NAME);
        GraphLayers gls = lb.getGraphLayers();
        out.print(gls.report());

        assertEquals("Wrong layer for nsample1", 0, gls.getLayer(pip.nsample1));
        assertEquals("Wrong layer for p11", 1, gls.getLayer(pip.p11));
        assertEquals("Wrong layer for nas1", 2, gls.getLayer(pip.nas1));
        assertEquals("Wrong layer for p2", 3, gls.getLayer(pip.p2));
        assertEquals("Wrong layer for nar1", 4, gls.getLayer(pip.nar1));
        assertEquals("Wrong layer for nar2", 4, gls.getLayer(pip.nar2));

        out.println("\n" + StringUtils.center(" /end: Testing layering of assay file only ", 120, "-") + "\n");

    }


    @Test
    public void testSeparatedSampleFile() {
        out.println("\n\n" + StringUtils.center(" Testing layering of sample file only ", 120, "-") + "\n");

        SeparatedFilesModel pip = new SeparatedFilesModel();
        pip.as1.addAnnotation(
                new Annotation(new AnnotationType(ProcessingEntityTabMapper.ASSAY_FILE_ANNOTATION_TAG),
                        FOO_ASSAY_FILE_NAME
                ));

        LayersBuilder lb = new LayersBuilder(new BIIObjectStore(), pip.study);
        GraphLayers gls = lb.getGraphLayers();
        out.print(gls.report());

        assertEquals("Wrong layer for src1", 0, gls.getLayer(pip.nsrc1));
        assertEquals("Wrong layer for src2", 0, gls.getLayer(pip.nsrc2));
        assertEquals("Wrong layer for p1", 1, gls.getLayer(pip.p1));
        assertEquals("Wrong layer for sample1", 2, gls.getLayer(pip.nsample1));

        out.println("\n" + StringUtils.center(" /end: Testing layering of sample file only ", 120, "-") + "\n");
    }


}
