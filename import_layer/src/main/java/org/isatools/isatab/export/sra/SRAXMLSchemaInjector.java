package org.isatools.isatab.export.sra;

/**
 * Created by IntelliJ IDEA.
 * User: prs
 * Date: 28/05/2012
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * SRAXMLSchemaInjector  copied from BioPortalXMLModifier adds the xmlnamespace tag to the file so that it can be parsed using
 * JaxB methods coupled with the xml schema
 *
 * @author eamonnmaguire
 * @date Feb 18, 2010
 */

public class SRAXMLSchemaInjector {
    
    private static final String STD_SRA_NAMESPACE = "xsi:noNamespaceSchemaLocation=\"ftp://ftp.sra.ebi.ac.uk/meta/xsd/sra_1_3/";
    
    public static File addNameSpaceToFile(File originalFile, String namespace, String openingXMLTag) {

        if (originalFile != null) {
            PrintStream outputStream = null;

            if (originalFile.exists()) {
                try {
                    File newFile = getNewFileName(originalFile);

                    outputStream = new PrintStream(newFile);

                    Scanner originalFileScanner = new Scanner(originalFile);
                    String line;
                    outputStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");


                    while (originalFileScanner.hasNextLine()) {
                        line = originalFileScanner.nextLine();
                        if (line.contains(openingXMLTag) && openingXMLTag.contains(">")) {
                            line = openingXMLTag.substring(0, openingXMLTag.indexOf(">")) + (line.contains("xmlns:xsi") ? " " : " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ") + STD_SRA_NAMESPACE + namespace + "\">";
                        }
                        else  if ( line.contains(openingXMLTag) && openingXMLTag.contains(" ")) {
                            
                            String insertnamespace = openingXMLTag+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""  + STD_SRA_NAMESPACE + namespace;
                            //line = openingXMLTag.substring(0, openingXMLTag.indexOf("\\s")) + (line.contains("xmlns:xsi") ? " " :  + "\">";
                         line.replaceFirst(openingXMLTag, insertnamespace)  ;
                            System.out.println("THIS IS NEW !!!!: "+line);

                        } 
                        outputStream.println(line);
                    }

                    return newFile;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                }
            }
        }
        return null;
    }

    private static File getNewFileName(File originalFile) {
        String filePath = originalFile.getAbsolutePath();

        filePath = filePath.substring(0, filePath.lastIndexOf(File.separator));

        return new File(filePath + File.separator + originalFile.getName().replaceFirst(".xml", "-final.xml") );
    }

    public static void main(String[] args) {
        addNameSpaceToFile(new File("/Users/prs/git/ValidatorConverterManager/ISAvalidator-ISAconverter-BIImanager/import_layer/target/export/sra/VS-454-MBL/run_set.xml"),
                "SRA.run.xsd", "<RUN_SET>");
    }


}
