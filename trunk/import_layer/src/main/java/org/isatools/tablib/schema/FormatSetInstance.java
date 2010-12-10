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

package org.isatools.tablib.schema;

import org.apache.commons.io.FileUtils;
import org.isatools.tablib.exceptions.TabInternalErrorException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An instance of a whole TAB format.
 * <p/>
 * <p><b>date</b>: Mar 3, 2008</p>
 *
 * @author brandizi
 */
public class FormatSetInstance implements TabInstanceEntity {
    private final FormatSet formatSet;
    private final List<FormatInstance> formatInstances = new ArrayList<FormatInstance>();

    public FormatSetInstance(FormatSet formatSet) {
        if (formatSet == null) {
            throw new TabInternalErrorException("Cannot have a format set instance of a null format set");
        }

        this.formatSet = formatSet;
    }

    public FormatSet getFormatSet() {
        return formatSet;
    }

    public List<FormatInstance> getFormatInstances() {
        return Collections.unmodifiableList(formatInstances);
    }

    public void addFormatInstance(FormatInstance instance) {
        if (instance == null) {
            throw new TabInternalErrorException("Cannot add a null format instance to a format set");
        }

        FormatSetInstance parent = instance.getParent();
        if (parent != null && parent != this) {
            throw new TabInternalErrorException(
                    "Attempt to add a format instance to a parent other than the one stored by the instance: "
                            + "format: " + instance.getFormat().getAttr("id")
            );
        }
        formatInstances.add(instance);
        if (parent == null) {
            instance.setParent(this);
        }
    }


    /**
     * Gets the first format instance having the parameter id
     */
    public FormatInstance getFormatInstance(String formatId) {
        for (FormatInstance instance : getFormatInstances()) {
            if (formatId.equals(instance.getFormat().getId())) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Gets the instances of a particular format
     */
    public List<FormatInstance> getFormatInstances(String formatId) {
        List<FormatInstance> result = new ArrayList<FormatInstance>();
        for (FormatInstance instance : getFormatInstances()) {
            if (formatId.equals(instance.getFormat().getId())) {
                result.add(instance);
            }
        }
        return result;
    }

    /**
     * A wrapper of
     * {@link #getFormatInstance(String) geFormatInstance( formatId )}.{@link FormatInstance#getSectionInstance(String) getSectionInstance( sectionId )}
     */
    public SectionInstance getSectionInstance(String formatId, String sectionId) {
        FormatInstance formatInstance = this.getFormatInstance(formatId);
        if (formatInstance == null) {
            throw new TabInternalErrorException(
                    "No format instance '" + formatId + "' found"
            );
        }
        return formatInstance.getSectionInstance(sectionId);
    }

    /**
     * Goes through all the format instances and section instances with the parameter identifiers
     */
    public List<SectionInstance> getSectionInstances(String formatId, String sectionId) {
        List<SectionInstance> sectionInstances = new ArrayList<SectionInstance>();
        for (FormatInstance formatInstance : formatInstances) {
            for (SectionInstance sectionInstance : formatInstance.getSectionInstances(sectionId)) {
                sectionInstances.add(sectionInstance);
            }
        }

        return sectionInstances;
    }


    /**
     * Dumps the format instance to a directory, in TSV format. Uses {@link FormatInstance#getFileId()} and produces a
     * file per format instance. So, you need to set a valid value for this property.
     * <p/>
     * TODO: we need to use opencsv and pass csvwriter rather than returning strings.
     *
     * @param path where to dump to
     */
    public void dump(String path) throws IOException {
        for (FormatInstance formatInstance : getFormatInstances()) {
            StringBuilder formatDump = formatInstance.dump();
            String fileId = formatInstance.getFileId();
            FileUtils.writeStringToFile(new File(path + "/" + fileId), formatDump.toString());
        }
    }
}
