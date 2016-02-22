package org.isatools.isatab_v1.validator;

import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;

public class ISATABNanoValidatorTest {

    @Test
    public void testValidateISATABNanoFile() throws IOException {

        out.println("\n\n" + StringUtils.center("Testing validator for ISATAB Nano case.", 120, "-") + "\n");

        String baseDir = System.getProperty("basedir");
        if ( baseDir == null )
        {
            baseDir = new File( "" ).getCanonicalPath();
        }
        String subDir = baseDir + "/target/test-classes/test-data/isatab/isatab_nano/Decuzzi";

        GUIISATABValidator validator = new GUIISATABValidator();

        GUIInvokerResult result = validator.validate(subDir);
        assertEquals("Validation should not have failed!", GUIInvokerResult.SUCCESS, result);

        System.out.println(validator.report());

        out.println("\n" + StringUtils.center("/end:Testing validator for ISATAB Nano case.", 120, "-") + "\n\n");
    }
}
