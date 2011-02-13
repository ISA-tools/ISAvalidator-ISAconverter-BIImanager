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

package org.isatools.tablib.export;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.BIIObjectStore;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Exports a whole format.
 * <p/>
 * date: Mar 31, 2008
 *
 * @author brandizi
 */
public abstract class FormatExporter extends ComposedTabExporter<FormatInstance, FormatComponentExporter<?>>
        implements FormatElementExporter<FormatInstance> {
    private final Map<String, List<String>> externalFilePaths = new HashMap<String, List<String>>();

    /**
     * @see AbstractTabExporter#TabExporter(BIIObjectStore)
     */
    public FormatExporter(BIIObjectStore store) {
        super(store);
    }

    /**
     * Returns a format set instance even in the case no exporter is provided by {@link #getExporters()}. This should
     * be useful to the derived classes. I ignore a return type from the exporter which I don't know (i.e. is not either
     * {@link SectionBlockInstance} or {@link SectionInstance}.
     */
    public FormatInstance export() {
        FormatInstance formatInstance = createFormatInstance();
        if (exporters == null || exporters.size() == 0) {
            log.trace("WARNING, The exporter" + getClass().getSimpleName() + " has no component exporter");
        } else {
            for (FormatComponentExporter<?> exporter : exporters) {
                FormatInstanceComponent formatInstanceComponent = exporter.export();
                if (formatInstanceComponent == null) {
                    log.trace("No result for the export of " + exporter.getClass().getSimpleName());
                    continue;
                } else if (formatInstanceComponent instanceof SectionInstance) {
                    SectionInstance sectionInstance = (SectionInstance) formatInstanceComponent;
                    log.trace("Adding exported section: " + sectionInstance.getSectionId());
                    formatInstance.addSectionInstance(sectionInstance);
                } else if (formatInstanceComponent instanceof SectionBlockInstance) {
                    SectionBlockInstance sectionBlockInstance = (SectionBlockInstance) formatInstanceComponent;
                    log.trace("Adding exported section block: " + sectionBlockInstance.getClass().getSimpleName());
                    List<SectionInstance> sectionInstances = sectionBlockInstance.getSectionInstances();
                    if (sectionInstances == null || sectionInstances.size() == 0) {
                        log.trace("No result for the export of sections in " + sectionBlockInstance.getClass().getSimpleName());
                    } else {
                        for (SectionInstance sectionInstance : sectionInstances) {
                            log.trace("Adding exported section: " + sectionInstance.getSectionId() + " (in section block)");
                            formatInstance.addSectionInstance(sectionInstance);
                        }
                    }
                } else {
                    log.trace("FormatExporter: I don't know what to do with the class " + formatInstanceComponent.getClass().getSimpleName()
                            + ", leaving it to my decendants.");
                }
            }
        } // for

        log.trace("Returning instance of format set: " + formatInstance.getFormat().getId());
        return formatInstance;
    }

    /**
     * Creates an instance of the kind of spreadsheet this exporter is about
     */
    protected abstract FormatInstance createFormatInstance();


    /**
     * If the field at fieldIndex refers to a file path. A field is about files if it has the ref-file-type attribute and
     * this has one of the values: raw, processed, generic. In such case returns a 3-sized array with:
     * field Header and the file path value, one of raw, processed, generic.
     * <p/>
     * if ref-file-type is not defined, returns null.
     */
    public static String[] getExternalFileValue(Record record, int fieldIndex) {
        Field field = record.getParent().getField(fieldIndex);
        String fileRefType = field.getAttr("ref-file-type");
        if (fileRefType == null) {
            return null;
        }

        String fieldHeader = field.dump().toString();
        String path = StringUtils.trimToNull(record.getString(fieldIndex));

        return new String[]{fieldHeader, path, fileRefType};
    }


    /**
     * Collect those values that are file paths. This is achieved from the field specification in the TAB format description,
     * by means of the "ref-file-type" attribute.
     */
    public static void addExternalFilePath(
            Map<String, List<String>> result, Record record, int fieldIndex
    ) {
        String[] extFileValue = getExternalFileValue(record, fieldIndex);
        if (extFileValue == null) {
            return;
        }

        String fieldHeader = extFileValue[0], path = extFileValue[1];
        if (path == null) {
            return;
        }

        List<String> filePaths = result.get(fieldHeader);
        if (filePaths == null) {
            filePaths = new ArrayList<String>();
            result.put(fieldHeader, filePaths);
        }
        filePaths.add(path);
        return;
    }

    /**
     * A wrapper that uses local status for storing results
     */
    protected void addExternalFilePath(
            Record record, int fieldIndex
    ) {
        addExternalFilePath(this.externalFilePaths, record, fieldIndex);
    }


    /**
     * Reset the file paths that was previously via {@link #addExternalFilePath(String, String) addExternalFilePath()}.
     */
    protected void clearExternalFilePaths() {
        externalFilePaths.clear();
    }

    /**
     * Returns all the files found as values of a certain field that has been previously set via
     * {@link #addExternalFilePath(String, String) addExternalFilePath()}.
     *
     * @param fileFieldName the name of the field the file paths/names come from.
     * @return a list of file paths/names.
     */
    public List<String> getExternalFilePaths(String fileFieldName) {
        List<String> result = this.externalFilePaths.get(fileFieldName);
        if (result == null) {
            result = Collections.emptyList();
        } else {
            result = Collections.unmodifiableList(result);
        }
        return result;
    }

    /**
     * @return the headers of those fields that was recognized as data file reference fields (via 'is-file-ref' in the
     *         format definition file). This will contain something only after having called {@link #clearExternalFilePaths()}
     */
    public Set<String> getExternalFileFieldHeaders() {
        return externalFilePaths.keySet();
    }

    /**
     * Dispatch the submission files present in the format into a target path. This is an helper for dealing with
     * external data files during tabular exports.
     */
    public void dispatchFiles(String sourcePath, String outPath) throws IOException {
        // All the external files
        for (String fileType : getExternalFileFieldHeaders()) {
            for (String filePath : getExternalFilePaths(fileType)) {
                File file = new File(sourcePath + "/" + filePath);
                if (!file.exists()) {
                    log.warn("WARNING, Source file '" + filePath + "' not found");
                    continue;
                }
                log.info("Dispatching file '" + fileType + "' / '" + filePath + "'");
                FileUtils.copyFileToDirectory(file, new File(outPath), true);
            }
        }
    }


}
