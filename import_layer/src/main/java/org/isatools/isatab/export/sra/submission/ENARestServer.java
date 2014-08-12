package org.isatools.isatab.export.sra.submission;

/**
 * Created by agbeltran
 */
public enum ENARestServer {

    DEV,
    TEST,
    PROD;

    public String getURL() {
        switch (this) {
            case DEV:
                return "https://wwwdev.ebi.ac.uk";
            case TEST:
                return "https://www-test.ebi.ac.uk";
            case PROD:
                return "https://www.ebi.ac.uk";
            default:
                return "";
        }
    }

    public static final String PATH = "/ena/submit/drop-box/submit/";

}