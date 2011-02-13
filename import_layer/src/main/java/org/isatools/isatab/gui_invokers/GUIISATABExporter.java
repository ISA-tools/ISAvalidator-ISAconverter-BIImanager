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

package org.isatools.isatab.gui_invokers;

import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import org.isatools.isatab.export.isatab.ISATABDBExporter;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Study;

import java.util.Collection;

/**
 * The invoker to be used by the GUI interface for the ISATAB exporter component.
 * <p/>
 * See {@link AbstractGUIInvokerWithStudySelection} for methods about the selection of studies to be
 * exported.
 *
 * @author brandizi
 *         <b>date</b>: Jul 24, 2009
 */
public class GUIISATABExporter extends AbstractGUIInvokerWithStudySelection {

    public GUIISATABExporter() {
        super();
        AbstractImportLayerShellCommand.setupLog4JPath(System.getProperty("user.dir") + "/isatab_export.log");
        initEntityManager();
    }


    /**
     * Exports the parameter studies to the BII repository. More precisely:
     * <p/>
     * <ul>
     * <li> meta-data files are recreated from the DB, a copy is placed into the study's meta-data directory,
     * original copies are kept into original/, under the same meta-data directory.</li>
     * <li>if skipOldDataFilesBackup is false, all the data files that are not referenced by the new ISATAB meta-data
     * spreadsheets are backup into original/, under the same data directory where the data file initially was. In case
     * that flag is true, only warnings are issued for these files, leaving them untouched.</li>
     * </ul>
     */
    public GUIInvokerResult isatabExportToRepository(final Collection<Study> studies, boolean skipOldDataFilesBackup) {
        try {
            initEntityManager();
            DaoFactory daoFactory = DaoFactory.getInstance(this.entityManager);
            ISATABDBExporter isaExporter = new ISATABDBExporter(daoFactory, studies);
            isaExporter.exportToRepository(skipOldDataFilesBackup);
            vlog.info("\n\nExport Done, BII repository updated");
            return GUIInvokerResult.SUCCESS;
        }
        catch (Exception e) {
            vlog.error(e.getMessage(), e);
            return GUIInvokerResult.ERROR;
        }
    }

    /**
     * A wrapper of {@link #isatabExportToRepository(Collection, boolean) isatabExportToRepository ( studies, false ) }.
     */
    public GUIInvokerResult isatabExportToRepository(final Collection<Study> studies) {
        return isatabExportToRepository(studies, false);
    }

    /**
     * Exports the parameter studies to the specified path. Creates multiple directories, one per study or per investigation,
     * depending on whether the study belongs in an investigation or not (directories are named with investigation or study
     * accession in the two cases). Pour inside such directories the ISATAB meta-data files recreated from the BII DB contents.
     * In case skipDataFilesDump is false, also copies the data files under the same directories, this step is skipped otherwise.
     */
    public GUIInvokerResult isatabExportToPath(final Collection<Study> studies, String exportPath, boolean skipDataFilesDump) {
        try {
            initEntityManager();
            DaoFactory daoFactory = DaoFactory.getInstance(this.entityManager);
            ISATABDBExporter isaExporter = new ISATABDBExporter(daoFactory, studies);
            isaExporter.exportToPath(exportPath, skipDataFilesDump);
            vlog.info("\n\nExport Done to '" + exportPath + "'");
            return GUIInvokerResult.SUCCESS;
        }
        catch (Exception e) {
            vlog.error(e.getMessage(), e);
            return GUIInvokerResult.ERROR;
        }
    }

    /**
     * A wrapper of {@link #isatabExportToPath(Collection, String, boolean) isatabExportToPath ( studies, exportPath, false ) }
     */
    public GUIInvokerResult isatabExportToPath(final Collection<Study> studies, String exportPath) {
        return isatabExportToPath(studies, exportPath, false);
    }

}
