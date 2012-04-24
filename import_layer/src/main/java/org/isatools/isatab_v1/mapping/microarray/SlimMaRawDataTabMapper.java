package org.isatools.isatab_v1.mapping.microarray;

import org.isatools.isatab.mapping.attributes.AnnotationMappingHelper;
import org.isatools.tablib.mapping.pipeline.DataNodeTabMapper;
import org.isatools.tablib.mapping.properties.StringPropertyMappingHelper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.term.DataType;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 20/04/2012
 *         Time: 19:21
 */
public class SlimMaRawDataTabMapper extends DataNodeTabMapper {

    @SuppressWarnings("unchecked")
    public SlimMaRawDataTabMapper(BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, int endField) {
        super(store, sectionInstance, fieldIndex, endField);

        // "Array Data File" is the first field

        this.mappingHelpersConfig.put("Array Data Matrix File", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "dataMatrixUrl"}}
        ));

        this.mappingHelpersConfig.put("Image File", new MappingHelperConfig<AnnotationMappingHelper>(
                AnnotationMappingHelper.class, new String[][]{{"propertyName", "imageFile"}}
        ));

    }


    /**
     * Provides a new data object
     */
    @Override
    public Data newMappedObject() throws InstantiationException, IllegalAccessException {

        return new Data(
                "", new DataType("bii:microarray_raw_data", "Microarray Raw Data", new ReferenceSource("bii:data_types", "bii:data_types"))
        );
    }

}