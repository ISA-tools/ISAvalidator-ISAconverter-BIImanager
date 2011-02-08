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

import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.*;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.term.AnnotationType;
import uk.ac.ebi.bioinvindex.model.term.FactorValue;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.model.xref.Xref;
import uk.ac.ebi.bioinvindex.utils.AccessionGenerator;

import java.util.*;

/**
 * I only need this in order to recognize I am dealing with the mapping of a node (source, sample, data), instead of
 * the mapping of a protocol application.
 * <p/>
 * Feb 5, 2008
 *
 * @author brandizi
 */
public abstract class ProcessingNodeTabMapper<NT extends Identifiable>
        extends ProcessingEntityTabMapper<NT> {
    private final MappingUtils mappingUtils;

    public ProcessingNodeTabMapper(BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, int endField) {
        super(store, sectionInstance, fieldIndex, endField);
        mappingUtils = new MappingUtils(store);
    }


    /**
     * Changes the default behavior so that, when an helper has the option "lookAllHeaders" == "true", the helper's field
     * is searched over all the section (independently on the range), otherwise it is searched only in the specified range,
     * as usually).
     * <p/>
     * TODO: do we need to transfer this behavior to a more general level (all kind {@link ProcessingEntityTabMapper})?
     */
    @Override
    protected Map<Integer, PropertyMappingHelper<NT, ?>> getPropertyHelpers(int startFieldIndex, int endFieldIndex) {
        if (propertyMappers != null) {
            return Collections.unmodifiableMap(this.propertyMappers);
        }

        if (mappingHelpersConfig.size() == 0) {
            log.trace("WARNING, No helper defined for the the mapper " + this.getClass().getSimpleName());
            return null;
        }

        SectionInstance sectionInstance = getSectionInstance();
        propertyMappers = new HashMap<Integer, PropertyMappingHelper<NT, ?>>();
        List<Field> fields = sectionInstance.getFields();

        for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
            PropertyMappingHelper<NT, ?> helper = newPropertyHelper(fieldIndex);
            boolean isInRange = fieldIndex >= startFieldIndex && fieldIndex <= endFieldIndex;
            if (helper != null && ("true".equals(helper.getOptions().get("lookAllHeaders")) || isInRange)) {
                propertyMappers.put(fieldIndex, helper);
            }
        } // for

        return propertyMappers;
    }


    /**
     * Creates an assay associated to a mapped material. Can be used where necessary, i.e.: with materials like
     * Hybridization or MS/SPEC run, which are associated to the assays.
     */
    protected Assay createAssay(Material material) {
        AssayGroup assayGroup = mappingUtils.getAssayGroupFromSection(getSectionInstance());

        if (assayGroup == null) {
            log.error("ERROR No assay group associated to material " + material.getName() + "(" + material.getAcc()
                    + "), no assay will be created and this is probably an error");
            return null;
        }

        // TODO: study should never be null
        Study study = assayGroup.getStudy();
        if (study == null) {
            log.error("ERROR, no study associated to material " + material.getName() + "(" + material.getAcc()
                    + "), no assay will be created and this is probably an error");
        }

        Assay assay = new Assay();
        assay.setMeasurement(assayGroup.getMeasurement());
        assay.setTechnology(assayGroup.getTechnology());
        assay.setMaterial((Material) material);

        // Provides an accession
        String acc = "";
        String studyAcc = StringUtils.trimToNull(study.getAcc());
        if (studyAcc != null) {
            acc += studyAcc + ":";
        }
        String matName = material.getName();
        acc = AccessionGenerator.getInstance().generateAcc(acc + "assay:" + matName + ".");
        assay.setAcc(acc);

        // We do not need them in the store, since can be reached from the studies.
        // getStore ().put ( Assay.class, matName, assay );

        // Setup the platform
        assay.setAssayPlatform(assayGroup.getPlatform());

        // Clones the annotations from assayGroup
        Collection<Annotation> annotations = new ArrayList<Annotation>(assayGroup.getAnnotations());
        // And also from the material
        annotations.addAll(material.getAnnotations());
        for (Annotation ann : annotations) {
            // TODO: proper clone() method
            AnnotationType annType = ann.getType();
            String annTypeS = annType.getValue();
            Annotation annNew = new Annotation(new AnnotationType(annTypeS), ann.getText());
            assay.addAnnotation(annNew);
        }

        // After having done the annotations, set the AE/PRIDE Xrefs from them
        for (Annotation ann : assay.getAnnotations()) {
            for (Xref xref : getXrefs(ann)) {
                assay.addXref(xref);
            }
        }

        // TODO: Until we change(?) hashCode() it is *fundamental* that this is done *after* the assay is complete.
        study.addAssay(assay);
        // Same for AssayGroup
        assayGroup.addAssay(assay);
        getStore().put(Assay.class, acc, assay);

        return assay;
    }



    /**
     * Creates proper {@link Xref X-refs} corresponding to those comments about external web links and files.
     */
    private Xref[] getXrefs(Annotation ann) {
        AnnotationType annType = ann.getType();
        String annTypeS = annType.getValue();
        ReferenceSource source = null;
        Xref[] xrefs = new Xref[0];

        // Split the value if it is multi-valued, semicolon-separated.
        String annValStr = StringUtils.trimToNull(ann.getText());
        if (annValStr == null) {
            return xrefs;
        }

        //TODO: this is a patch needed to create links in the BII/webapp. We need to factorize this.
        //
        if (annTypeS.equals("comment:PRIDE Accession")) {
            // Setup the PRIDE Xref
            source = new ReferenceSource("Pride Web Site");
            source.setAcc("PRIDE:WEB");
            source.setUrl("http://www.ebi.ac.uk/pride");
        } else if (annTypeS.equals("comment:PRIDE Raw Data Accession")) {
            source = new ReferenceSource("PRIDE Raw Data Files");
            source.setAcc("PRIDE:RAW");
            // source.setUrl ( "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data" );
        } else if (annTypeS.equals("comment:PRIDE Processed Data Accession")) {
            source = new ReferenceSource("PRIDE Processed Data Files");
            source.setAcc("PRIDE:PROCESSED");
            //source.setUrl ( "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data" );
        } else if (annTypeS.equals("comment:ArrayExpress Accession")) {
            // Setup the AE Xref
            source = new ReferenceSource("ArrayExpress Experiment Web Page");
            source.setDescription("ArrayExpress Experiment Web Page");
            source.setAcc("AE:WEB");
            source.setUrl("http://www.ebi.ac.uk/microarray-as/aer/result?queryFor=Experiment&eAccession=");
        } else if (annTypeS.equals("comment:ArrayExpress Raw Data URL")) {
            source = new ReferenceSource("ArrayExpress Raw Data Files");
            source.setAcc("AE:RAW");
            source.setUrl("ftp://ftp.ebi.ac.uk/pub/databases/microarray/data");
        } else if (annTypeS.equals("comment:ArrayExpress Processed Data URL")) {
            source = new ReferenceSource("ArrayExpress Processed Data Files");
            source.setAcc("AE:PROCESSED");
            source.setUrl("ftp://ftp.ebi.ac.uk/pub/databases/microarray/data");
        } else if (annTypeS.equals("comment:ENA Accession")) {
            source = new ReferenceSource("ENA Web Site");
            source.setAcc("ENA:WEB");
        } else if (annTypeS.equals("comment:ENA Raw Data Accession")) {
            source = new ReferenceSource("ENA Raw Data Files");
            source.setAcc("ENA:RAW");
        } else if (annTypeS.equals("comment:ENA Processed Data Accession")) {
            source = new ReferenceSource("ENA Processed Data Files");
            source.setAcc("ENA:PROCESSED");
        } else if (annTypeS.equals("comment:EMBL Accession")) {
            source = new ReferenceSource("EMBL Web Site");
            source.setAcc("EMBL:WEB");
        } else if (annTypeS.equals("comment:EMBL Raw Data Accession")) {
            source = new ReferenceSource("ENA Raw Data Files");
            source.setAcc("EMBL:RAW");
        } else if (annTypeS.equals("comment:EMBL Processed Data Accession")) {
            source = new ReferenceSource("ENA Processed Data Files");
            source.setAcc("EMBL:PROCESSED");
        } else if (annTypeS.equals("comment:GEO Processed Data Accession")) {
            source = new ReferenceSource("GEO Raw Data Files");
            source.setAcc("GEO:PROCESSED");
        } else if (annTypeS.equals("comment:GEO Raw Data Accession")) {
            source = new ReferenceSource("GEO Raw Data Files");
            source.setAcc("GEO:RAW");
        } else if (annTypeS.equals("comment:GEO Accession")) {
            source = new ReferenceSource("GEO Experiment Web Page");
            source.setDescription("GEO Experiment Web Page");
            source.setAcc("GEO:WEB");
        } else if(annTypeS.equals("comment:Export")) {
            source = new ReferenceSource("Export to SRA");
            source.setDescription("To be used in SRA export");
            source.setAcc("EXPORT");
        }

        if (source == null)
        // We haven't any known comment that should generate an Xref, skip
        {
            return xrefs;
        }

        // New version of the web app needs the accession in the name
        source.setDescription(source.getName());
        source.setName(source.getAcc());

        // The annotation can have multiple values separated by ';'
        //
        String annVals[] = annValStr.split(";");
        xrefs = new Xref[annVals.length];
        for (int i = 0; i < annVals.length; i++) {
            xrefs[i] = new Xref(annVals[i]);
            xrefs[i].setSource(source);

        }

        return xrefs;
    }


    protected AssayResult createAssayResult(Data data) {
        AssayGroup assayGroup = mappingUtils.getAssayGroupFromSection(getSectionInstance());

        if (assayGroup == null) {
            log.error("No assay group associated to data " + data.getName() + "(" + data.getAcc()
                    + "), no assay will be created and this is probably an error");
            return null;
        }

        // TODO: study should never be null
        Study study = assayGroup.getStudy();
        if (study == null) {
            log.error("No study associated to data " + data.getName() + "(" + data.getAcc()
                    + "), no assay will be created and this is probably an error");
        }

        AssayResult ar = new AssayResult((Data) data, study);

        // Let's add factor values
        for (FactorValue fv : data.getFactorValues()) {
            ar.addCascadedPropertyValue(fv);
        }

        return ar;
    }


    /**
     * Gets the material/data name. This is useful in {@link #map(int)}, because the name is needed
     * in advance for certain operations (e.g.: defining the accession). Default version just gets the field in
     * {@link #getFieldIndex()}.
     */
    protected String getName(int recordIndex, NT mappedObject) {
        SectionInstance sectionInstance = getSectionInstance();
        return StringUtils.trimToEmpty(sectionInstance.getString(recordIndex, getFieldIndex()));
    }


}
