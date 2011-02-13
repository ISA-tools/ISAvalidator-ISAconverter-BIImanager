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

package org.isatools.tablib.mapping.pipeline;


import org.isatools.isatab.mapping.attributes.CommentMappingHelper;
import org.isatools.tablib.mapping.ClassTabMapper;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Annotatable;
import uk.ac.ebi.bioinvindex.model.Annotation;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.term.AnnotationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Maps a processing entity from a SDRF-like section in the TAB, such as a Material, a Data, a Protocol REF.
 * For the moment this class does quite a minimal job (finds the header about the mapped entity).
 * <p/>
 * Jan 22, 2008
 *
 * @author brandizi
 * @param <NT> the node type, returned by the {@link #map(int)} method.
 */
public abstract class ProcessingEntityTabMapper<NT extends Identifiable>
        extends ClassTabMapper<NT> {
    private final int fieldIndex, endFieldIndex;
    public static final String
            SAMPLE_FILE_ANNOTATION_TAG = "sampleFileId",
            ASSAY_FILE_ANNOTATION_TAG = "assayFileId";

    @SuppressWarnings("unchecked")
    public ProcessingEntityTabMapper(BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, int endFieldIndex) {
        super(store, sectionInstance);
        this.fieldIndex = fieldIndex;
        this.endFieldIndex = endFieldIndex;

        /* This is usually present in any node */
        this.mappingHelpersConfig.put("Comment", new MappingHelperConfig<CommentMappingHelper>(
                CommentMappingHelper.class
        ));
    }


    /**
     * The field range where this source is represented, doesn't include the fields {@link #getEndFieldNames()}
     */
    public int getEndFieldIndex() {
        return endFieldIndex;
    }


    /**
     * The lower bound where this mapper is supposed to start, i.e.: anything about this mapper is searched
     */
    public int getFieldIndex() {
        return fieldIndex;
    }


    /**
     * Restricts the range to {@link #getStartFieldIndex()} + 1 and {@link #getEndFieldIndex()}.
     */
    public Map<Integer, PropertyMappingHelper<NT, ?>> getPropertyHelpers() {
        return getPropertyHelpers(getFieldIndex() + 1, getEndFieldIndex());
    }


    /**
     * null, we don't need to store this
     */
    @Override
    public String getStoreKey(NT mappedObject) {
        return null;
    }


    /**
     * Doesn't do anything. This mapper is used by the processing mapper and the generic mapping
     * is not needed there.
     */
    public BIIObjectStore map() {
        return mapNothing();
    }


    /**
     * Adds the "spreadsheetFileId" annotation, about the file this element comes from, as it is returned by
     * {@link SectionInstance#getFileId()}.
     */
    @Override
    public NT map(int recordIndex) {
        NT result = super.map(recordIndex);
        addSourceFileAnnotation(result);
        return result;
    }

    /**
     * Add the file a mapped object comes from and its type (sample/assay). This is invoked in {@link #map(int)}.
     * You should call it in case you implement your own version of the map() method,
     */
    protected void addSourceFileAnnotation(NT mappedObject) {
        if (!(mappedObject instanceof Annotatable)) {
            return;
        }

        SectionInstance sectionInstance = getSectionInstance();
        String fileId = sectionInstance.getFileId();
        String formatId = sectionInstance.getParent().getFormat().getId();
        AnnotationType annType = new AnnotationType("study_samples".equals(formatId)
                ? SAMPLE_FILE_ANNOTATION_TAG
                : ASSAY_FILE_ANNOTATION_TAG
        );

        ((Annotatable) mappedObject).addAnnotation(new Annotation(annType, fileId));
    }


    /**
     * Collects the fields matched by the property mappers, adds up {@link #getFieldIndex()} anyway.
     */
    @Override
    public List<Integer> getMatchedFieldIndexes() {
        List<Integer> result = new ArrayList<Integer>();
        result.add(getFieldIndex());
        for (PropertyMappingHelper<?, ?> propertyHelper : getPropertyHelpers().values()) {
            result.addAll(propertyHelper.getMatchedFieldIndexes());
        }
        return result;
    }

    /**
     * @return true, because the validation must be delegated to {@link ProcessingsTabMapper} and the mappers
     *         used by it are not able to determine if their range of fields is valid or not.
     */
    @Override
    public boolean validateHeaders() {
        return true;
    }

}