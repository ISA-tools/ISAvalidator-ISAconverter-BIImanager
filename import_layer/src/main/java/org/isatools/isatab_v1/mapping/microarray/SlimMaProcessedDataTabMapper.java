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
 *         Date: 20/04/2012
 *         Time: 19:22
 */
public class SlimMaProcessedDataTabMapper extends DataNodeTabMapper {

    @SuppressWarnings("unchecked")
    public SlimMaProcessedDataTabMapper(BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, int endField) {

        // "Derived Array Data File" is the starting node
        super(store, sectionInstance, fieldIndex, endField);
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
        return new Data(
                "", new DataType("bii:microarray_derived_data", "Microarray Derived Data", new ReferenceSource("bii:data_types", "bii:data_types"))
        );
    }

}