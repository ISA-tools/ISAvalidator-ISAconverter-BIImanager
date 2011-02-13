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

package org.isatools.isatab.mapping;

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;

/**
 * A wrapper of Study. We need this here in the mapping process for two reasons:
 * 1) because we need to store the study file name (TODO: move it to {@link Study}?)
 * 2) because we need to retrieve studies by study file name, so that I put duplicates in the {@link BIIObjectStore} (i.e.:
 * {@link Study} instances and {@link StudyWrapper} instances), I use the {@link StudyWrapper} instances only to
 * retrieve the study when I have the study file name only.
 * <p/>
 * date: Mar 14, 2008
 *
 * @author brandizi
 */
public class StudyWrapper extends Identifiable {
    private Study study;
    private String fileId;

    public StudyWrapper(Study study) {
        super();
        this.setStudy(study);
    }

    public StudyWrapper(Study study, String fileId) {
        this(study);
        this.setFileId(fileId);
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }


    public static String findSampleFile(BIIObjectStore store, Study study) {
        for (StudyWrapper studyWrapper : store.valuesOfType(StudyWrapper.class)) {
            if (studyWrapper.getStudy() == study) {
                return studyWrapper.getFileId();
            }
        }
        return null;
    }

    /**
     * TODO: move to the model?
     */
    public static String getLabel(Study study) {
        String title = StringUtils.trimToEmpty(StringUtils.substring(study.getTitle(), 0, 15));
        String acc = StringUtils.trimToEmpty(study.getAcc());
        if (title.length() != 0) {
            if (acc.length() != 0) {
                return title + " (" + acc + ")";
            } else {
                return title;
            }
        } else {
            return acc.length() != 0 ? acc : "?";
        }
    }

    public String getLabel() {
        return getLabel(study);
    }

}
