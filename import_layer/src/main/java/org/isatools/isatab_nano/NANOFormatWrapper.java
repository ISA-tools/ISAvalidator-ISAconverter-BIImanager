package org.isatools.isatab_nano;

public class NANOFormatWrapper {

    public static void processFileHeader(String[] headers) {
        for (int headerIndex = 0; headerIndex < headers.length; headerIndex++) {
            for (NanoHeaders header : NanoHeaders.values()) {
                if (headers[headerIndex].startsWith(header.field)) {
                    headers[headerIndex] = "Comment[" + headers[headerIndex] + "]";
                }
            }
        }
    }

    enum NanoHeaders {
        MATERIAL_FILE("Material File"), MEASUREMENT_VALUE("Measurement Value"), STUDY_DISEASE("Study Disease"),
        STUDY_DISEASE_SOURCE("Study Disease Term Source REF"), STUDY_DISEASE_ACCESSION("Study Disease Term Accession Number"),
        STUDY_OUTCOME("Study Outcome"), STUDY_FILE_DESCRIPTION("Study File Description"), STUDY_ASSAY_NAME("Study Assay Measurement Name"),
        STUDY_ASSAY_NAME_SOURCE("Study Assay Measurement Name Term Source REF"), STUDY_ASSAY_NAME_ACCESSION("Study Assay Measurement Name Term Accession Number"),
        INVESTIGATION_DISEASE("Investigation Disease"), INVESTIGATION_DISEASE_SOURCE("Investigation Disease Term Source REF"),
        INVESTIGATION_DISEASE_ACCESSION("Investigation Disease Term Accession Number"), INVESTIGATION_OUTCOME("Investigation Outcome");

        private String field;

        NanoHeaders(String field) {
            this.field = field;
        }

        @Override
        public String toString() {
            return field;
        }
    }
}
