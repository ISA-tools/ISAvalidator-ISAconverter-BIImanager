package org.isatools.isatab.export.sra;

/**
 * Created by IntelliJ IDEA.
 * User: Philippe
 * Date: 27/01/2011
 * Time: 11:51
 * To change this template use File | Settings | File Templates.
 */
public enum SequencingProperties {

    SEQUENCING_PLATFORM("sequencing instrument"), LIBRARY_LAYOUT("library layout");
    private String value;

    SequencingProperties(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
