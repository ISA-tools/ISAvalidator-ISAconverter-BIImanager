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

package org.isatools.isatab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.isatools.isatab.export.DataFilesDispatcher;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import uk.ac.ebi.bioinvindex.dao.StudyDAO;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.unloading.StudyUnloader;
import uk.ac.ebi.bioinvindex.unloading.UnloadManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Persists an instance of the ISATAB format into the BII DB.
 * <p/>
 * <dl><dt>date:</dt><dd>Apr 23, 2008</dd></dl>
 *
 * @author brandizi
 */
public class ISATABUnloader {
    private UnloadManager unloadMgr;
    private final DaoFactory daoFactory;
    private String studyAcc;

    protected static final Log log = LogFactory.getLog(ISATABPersister.class);


    public ISATABUnloader(DaoFactory daoFactory, Timestamp submissionTs) {
        this.daoFactory = daoFactory;
        unloadMgr = new UnloadManager(daoFactory, submissionTs);
    }

    public ISATABUnloader(DaoFactory daoFactory, long submissionTsL) {
        this(daoFactory, new Timestamp(submissionTsL));
    }

    public ISATABUnloader(DaoFactory daoFactory, String studyAcc) {
        this.daoFactory = daoFactory;
        this.studyAcc = studyAcc;
    }


    public void unload() {
        List<Study> studies = new LinkedList<Study>();

        EntityManager emgr = daoFactory.getEntityManager();
        Session session = (Session) emgr.getDelegate();
        EntityTransaction ts = emgr.getTransaction();

        if (studyAcc != null) {
            StudyDAO dao = daoFactory.getStudyDAO();
            Study study = dao.getByAcc(studyAcc);
            if (study == null) {
                log.warn("Study with accession '" + studyAcc + "' not found, no undeletion performed.");
                return;
            }
            studies.add(study);

            unloadMgr = new UnloadManager(daoFactory, study.getSubmissionTs());
            StudyUnloader unloader = (StudyUnloader) unloadMgr.getUnloader(Study.class);
            unloader.queueByAcc(studyAcc);
        } else {
            studies.addAll(daoFactory.getStudyDAO().getBySubmissionTs(unloadMgr.getSubmissionTs()));
            unloadMgr.queueAll(studies);
        }

        try {
            if (!ts.isActive()) {
                ts.begin();
            }
            unloadMgr.delete();
            ts.commit();
        }
        catch (HibernateException e) {
            if (ts.isActive()) {
                ts.rollback();
            }
            throw new TabInternalErrorException("Error while performing the unloading:" + e.getMessage());
        }
        finally {
            session.flush();
        }

        DataFilesDispatcher fileDispatcher = new DataFilesDispatcher(daoFactory.getEntityManager());
        fileDispatcher.undispatch(studies);
    }

    public Set<String> getMessages() {
        return unloadMgr == null ? null : unloadMgr.getMessages();
    }
}
