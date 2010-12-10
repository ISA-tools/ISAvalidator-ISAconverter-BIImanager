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
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.isatab_v1.mapping.ISATABReducedMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.dao.StudyDAO;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Study;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collection;
import java.util.Properties;

/**
 * Additionally to {@link AbstractGUIInvoker}, contains functions for selecting studies to process
 *
 * @author brandizi
 *         <b>date</b>: Mar 8, 2010
 */
public abstract class AbstractGUIInvokerWithStudySelection extends AbstractGUIInvoker {
    protected Collection<Study> retrievedStudies = null;
    protected EntityManager entityManager;
    protected Properties hibernateProperties;

    public AbstractGUIInvokerWithStudySelection() {
        initEntityManager();
    }


    protected void initEntityManager() {
        hibernateProperties = AbstractImportLayerShellCommand.getHibernateProperties();
        hibernateProperties.setProperty("hibernate.search.indexing_strategy", "event");
        // We assume this, since we are unloading something that is supposed to already exist (including during tests).
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hbm2ddl.drop", "false");

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("BIIEntityManager", hibernateProperties);
        this.entityManager = entityManagerFactory.createEntityManager();
    }


    /**
     * Gets the retrievedStudies which were previously downloaded by {@link #loadSubmission(String)} or {@link #loadStudiesFromDB()}
     */
    public Collection<Study> getRetrievedStudies() {
        return retrievedStudies;
    }

    public Properties getHibernateProperties() {
        return hibernateProperties;
    }


    /**
     * Studies can be loaded from the BII DB and then you can ask the user which ones
     * he/she want to select. Result of this operation is {@link #getRetrievedStudies()}, which is a modifiable
     * collection (i.e.: you can remove unselected items from it).
     * <p/>
     * PLEASE NOTE: this method does not set {@link #getStore()}.
     */
    public GUIInvokerResult loadStudiesFromDB() {
        try {
            StudyDAO dao = DaoFactory.getInstance(entityManager).getStudyDAO();
            retrievedStudies = dao.getAll();
            return GUIInvokerResult.SUCCESS;
        }
        catch (Exception e) {
            vlog.error(e.getMessage(), e);
            return GUIInvokerResult.ERROR;
        }
    }


    /**
     * You may want to load study accessions from an ISATAB archive and then ask the user to tick the ones he/she
     * want to select
     * <p/>
     * TODO: is it useful? I've never used it yet nor tested.
     */
    public GUIInvokerResult loadSubmission(final String isatabSubmissionPath) {
        try {
            appender.clear();

            ISATABLoader isatabLoader = new ISATABLoader(isatabSubmissionPath);
            FormatSetInstance isatabInstance = isatabLoader.load();
            BIIObjectStore store = new BIIObjectStore();

            ISATABReducedMapper isatabMapper = new ISATABReducedMapper(store, isatabInstance);
            isatabMapper.map();

            retrievedStudies = store.valuesOfType(Study.class);

            return GUIInvokerResult.SUCCESS;
        }
        catch (Exception e) {
            vlog.error(e.getMessage(), e);
            return GUIInvokerResult.ERROR;
        }
    }

}
