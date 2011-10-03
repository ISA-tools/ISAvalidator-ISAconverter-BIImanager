// TODO: Remove
///**
//
// The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)
//
// Exhibit A
// The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
// 1.1/GPL version 2.0/LGPL version 2.1
//
// "The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"). You may not use this file except in compliance with the License.
// You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.
//
// The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
// Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
// http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
// 2007-2011 ISA Team. All Rights Reserved.
//
// Contributor(s):
// Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
// Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
// supporting standards-compliant experimental annotation and enabling curation at the community level.
// Bioinformatics 2010;26(18):2354-6.
//
// Alternatively, the contents of this file may be used under the terms of either the GNU General
// Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
// http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
// or the LGPL are applicable instead of those above. If you wish to allow use of your version
// of this file only under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your decision by deleting
// the provisions above and replace them with the notice and other provisions required by the
// GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
// of this file under the terms of any one of the MPL, the GPL or the LGPL.
//
// Sponsors:
// The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
// (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
// (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).
//
// */
//
//package org.isatools.isatab.export.isatab.pipeline;
//
//import org.apache.commons.lang.StringUtils;
//import org.junit.Test;
//import uk.ac.ebi.bioinvindex.model.processing.DataNode;
//import uk.ac.ebi.bioinvindex.model.processing.MaterialProcessing;
//import uk.ac.ebi.bioinvindex.model.processing.Processing;
//
//import static java.lang.System.out;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//public class GraphLayersTest {
//    @Test
//    public void testBasics() {
//        out.println("\n\n" + StringUtils.center(" Testing GraphLayers, basics ", 120, "-") + "\n");
//
//        GraphLayers gls = new GraphLayers();
//
//        DataNode dn1 = new DataNode(null);
//        dn1.setAcc("dn1");
//
//        DataNode dn2 = new DataNode(null);
//        dn2.setAcc("dn2");
//
//        DataNode dn3 = new DataNode(null);
//        dn3.setAcc("dn3");
//
//        gls.setLayer(dn1, 0);
//        gls.setLayer(dn2, 2);
//        gls.setLayer(dn1, 1);
//        gls.setLayer(dn3, -1);
//
//        assertEquals("Rats! Wrong layer for dn1!", 1, gls.getLayer(dn1));
//        assertEquals("Rats! Wrong layer for dn2!", 2, gls.getLayer(dn2));
//        assertTrue("Rats! Layer 2 should contain dn2!", gls.containsGraphEl(2, dn2));
//        assertTrue("Rats! Layer 1 should contain dn1!", gls.getGraphEls(1).contains(dn1));
//        assertEquals("Rats! dn3 should not be in!", -1, gls.getLayer(dn3));
//
//        out.println("\n" + StringUtils.center(" /end: Testing GraphLayers, basics ", 120, "-") + "\n");
//
//    }
//
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void testInserLayer() {
//        out.println("\n\n" + StringUtils.center(" Testing GraphLayers, insertLayer() ", 120, "-") + "\n");
//
//        GraphLayers gls = new GraphLayers();
//
//        DataNode dn1 = new DataNode(null);
//        dn1.setAcc("dn1");
//
//        DataNode dn2 = new DataNode(null);
//        dn2.setAcc("dn2");
//
//        DataNode dn3 = new DataNode(null);
//        dn3.setAcc("dn3");
//
//        Processing proc1 = new MaterialProcessing(null);
//        proc1.setAcc("proc1");
//
//        Processing proc2 = new MaterialProcessing(null);
//        proc2.setAcc("proc2");
//
//        gls.setLayer(dn1, 0);
//        gls.setLayer(dn2, 2);
//        gls.setLayer(dn1, 1);
//        gls.setLayer(dn3, -1);
//        gls.setLayer(proc1, 2);
//        gls.setLayer(proc2, 3);
//        gls.insertLayer(2);
//
//        // Should be 1:dn1 3:dn2,proc1, 4:proc2
//
//        assertEquals("Rats! Wrong layer for dn1!", 1, gls.getLayer(dn1));
//        assertEquals("Rats! Wrong layer for dn2!", 3, gls.getLayer(dn2));
//        assertTrue("Rats! Layer 2 should contain dn2!", gls.containsGraphEl(3, dn2));
//        assertTrue("Rats! Layer 1 should contain dn1!", gls.getGraphEls(1).contains(dn1));
//        assertEquals("Rats! dn3 should not be in!", -1, gls.getLayer(dn3));
//        assertEquals("Rats! proc1 should be in 3!", 3, gls.getLayer(proc1));
//        assertEquals("Rats! proc2 should be in 4!", 4, gls.getLayer(proc2));
//        assertTrue("Rats! Layer 3 should contain dn2!", gls.getGraphEls(3).contains(dn2));
//
//        out.println("\n" + StringUtils.center(" /end: Testing GraphLayers, insertLayer() ", 120, "-") + "\n");
//
//    }
//}
