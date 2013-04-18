package org.isatools.isatab.isaconfigurator.validators;

import org.apache.log4j.Logger;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;

import java.util.HashSet;
import java.util.Set;

/**
 * This code checks if all Assays have their sample name defined in the Study Sample file.
 * @author: Eamonn Maguire (eamonnmag@gmail.com)
 */
public class SampleNameValidator {

    protected static final Logger log = Logger.getLogger(SampleNameValidator.class);

    public GUIInvokerResult validate(SectionInstance studySampleTable, AssayGroup assayGroup) {

        SectionInstance assayTable = assayGroup.getAssaySectionInstance();

        Field studySampleNameField = studySampleTable.getFieldByHeader("Sample Name");
        Field assaySampleNameField = assayTable.getFieldByHeader("Sample Name");

        Set<String> assaySamples = new HashSet<String>();
        Set<String> studySamples = new HashSet<String>();

        getFields(assayTable, assaySampleNameField, assaySamples);
        getFields(studySampleTable, studySampleNameField, studySamples);

        boolean errors = false;
        for(String assaySample : assaySamples) {
            if(!studySamples.contains(assaySample)) {
                errors = true;
                log.error(String.format("%s is a Sample Name in %s, but it is not defined in the Study Sample File.", assaySample, assayGroup.getFilePath()));
            }
        }

        return errors ? GUIInvokerResult.ERROR : GUIInvokerResult.SUCCESS;
    }

    private void getFields(SectionInstance table, Field field, Set<String> set) {
        for(Record assayRecord : table.getRecords()) {
            set.add(assayRecord.get(field.getIndex()).toString());
        }
    }
}
