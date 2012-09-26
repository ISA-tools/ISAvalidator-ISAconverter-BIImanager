package org.isatools.isatab_v1.mapping.microarray;

import org.isatools.isatab.mapping.assay_common.attributes.AssayResultFVMappingHelper;
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
 *         Date: 26/09/2012
 *         Time: 14:42
 */
public class SlimMaNormalizationNameTabMapper extends DataNodeTabMapper {

    @SuppressWarnings("unchecked")
    public SlimMaNormalizationNameTabMapper(BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, int endField) {
        super(store, sectionInstance, fieldIndex, endField);

        // "Normalization Name" is the first column.

        this.mappingHelpersConfig.put("Derived Array Data File", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "url"}}
        ));
        this.mappingHelpersConfig.put("Derived Array Data Matrix File", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "dataMatrixUrl"}}
        ));
        this.mappingHelpersConfig.put("Factor Value",
                new MappingHelperConfig<AssayResultFVMappingHelper>(
                        AssayResultFVMappingHelper.class, new String[][]{{"lookAllHeaders", "true"}})
        );

    }


    /**
     * Provides a new data object
     */
    @Override
    public Data newMappedObject() throws InstantiationException, IllegalAccessException {
        // TODO: we need proper constants for the roles
        // TODO: fix to use real ReferenceSource
        //
        return new Data(
                "", new DataType("bii:microarray_normalized_data", "Microarray Normalized Data", new ReferenceSource("bii:data_types", "bii:data_types"))
        );
    }
}
