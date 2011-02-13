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
import org.isatools.tablib.schema.SectionInstance;
import uk.ac.ebi.bioinvindex.model.Accessible;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.term.AssayTechnology;
import uk.ac.ebi.bioinvindex.model.term.Measurement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Used to map the "Study Assays" section in the Investigation spreadsheet
 * <p/>
 * TODO: move to model?
 * <p/>
 * Mar 5, 2008
 *
 * @author brandizi
 */
public class AssayGroup extends Accessible {
    private Study study;
    private Measurement measurement;
    private AssayTechnology technology;
    private String platform;
    private String filePath;
    private Collection<Assay> assays = new HashSet<Assay>();

    private SectionInstance assaySectionInstance;
    private SectionInstance sampleSectionInstance;


    public AssayGroup() {
        super();
    }

    public AssayGroup(Study study) {
        super();
        setStudy(study);
    }


    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Measurement measurement) {
        this.measurement = measurement;
    }

    public AssayTechnology getTechnology() {
        return technology;
    }

    /**
     * A facility that returns {@link #getTechnology()}.getName() if technology is not null, "" otherwise.
     * The name is also passed to {@link StringUtils#trimToEmpty(String)} before returning.
     */
    public String getTechnologyName() {
        return technology == null ? "" : StringUtils.trimToEmpty(technology.getName());
    }

    public void setTechnology(AssayTechnology technology) {
        this.technology = technology;
    }

    /**
     * As per specification, this is reported in the assay group section and need to be transfered to the
     * assays.
     */
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }


    /**
     * The file path of the assay file for this assay group. It is used as unique identifier
     */
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * The section instance where the assays about this group are read from. It is used for the dispatching process
     */
    public SectionInstance getAssaySectionInstance() {
        return assaySectionInstance;
    }

    public void setAssaySectionInstance(SectionInstance assaySectionInstance) {
        this.assaySectionInstance = assaySectionInstance;
    }


    /**
     * The samples file associated to the study linked to this assay group.
     */
    public SectionInstance getSampleSectionInstance() {
        return sampleSectionInstance;
    }

    public void setSampleSectionInstance(SectionInstance sampleSectionInstance) {
        this.sampleSectionInstance = sampleSectionInstance;
    }

    public Collection<Assay> getAssays() {
        return Collections.unmodifiableCollection(assays);
    }

    protected void setAssays(Collection<Assay> assays) {
        this.assays = assays;
    }

    public boolean removeAssay(Assay assay) {
        return this.assays.remove(assay);
    }

    public void addAssay(Assay assay) {
        this.assays.add(assay);
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AssayGroup:{");
        sb.append(" acc:= ");
        sb.append(acc);
        sb.append(" annotations:= ");
        sb.append(getAnnotations());
        sb.append(" study:= ");

        if (study == null) {
            sb.append("null");
        } else {
            sb.append("[id:= " + study.getId()).append(" title:= ").append(study.getTitle()).append("]");
        }

        sb.append(" measurement:= ");
        sb.append(measurement);
        sb.append(" technology:= ");
        sb.append(technology);
        sb.append(" filePath:= ");
        sb.append(filePath);
        sb.append("}");
        return sb.toString();
    }

}
