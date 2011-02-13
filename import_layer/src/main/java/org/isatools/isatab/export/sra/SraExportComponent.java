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

package org.isatools.isatab.export.sra;

import org.apache.log4j.Logger;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabNDC;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic structure for a component the SRA exporter is made of.
 * <p/>
 * The {@link SraExporter} class is actually one big container of subroutines that all work with variables defined
 * here.
 * <p/>
 * For practical reasons this container is split into a chain of multiple classes, i.e.: the hierarchy that is rooted by
 * this class has not a logical rationale, the only reason it was defined for is that Java doesn't allow to spread a
 * single class over multiple files.
 *
 * @author brandizi
 *         <b>date</b>: Jul 20, 2009
 */
abstract class SraExportComponent {
    protected final BIIObjectStore store;
    protected final String sourcePath;
    protected final String exportPath;
    /**
     * All the SRA documents are headed with these
     */
    protected String centerName = null, brokerName = null;

    protected static final Logger log = Logger.getLogger(SraExporter.class);
    protected static final TabNDC ndc = TabNDC.getInstance();

    /**
     * Collect here those messages that occur many times, they will be printed only once, at the end of the
     * export procedure.
     */
    protected final Set<String> nonRepeatedMessages = new HashSet<String>();

    /**
     * @param store      the object store that contains the BII model to be converted into SRA submissions
     * @param sourcePath the directory of the ISATAB submission
     * @param exportPath the directory where the SRA files are saved. We create the "sra/" directory under this path.
     *                   We further create one submission per ISATAB study, naming it with the study accession (e.g.: export/sra/BII-S-4/).
     *                   In each of these directories you'll have a set of SRA files corresponding to one submission and to the BII study.
     */
    protected SraExportComponent(final BIIObjectStore store, final String sourcePath, final String exportPath) {
        this.store = store;
        this.sourcePath = sourcePath;
        this.exportPath = exportPath + "/sra";
    }

}
