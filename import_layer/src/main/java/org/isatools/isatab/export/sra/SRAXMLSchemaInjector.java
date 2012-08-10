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
                        else  if ( line.contains(openingXMLTag) && openingXMLTag.contains("alias")) {
                            
                            String insertnamespace = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "  + STD_SRA_NAMESPACE + namespace + "\"" + " alias";
                        line= line.replaceFirst("alias", insertnamespace)  ;

                        }
                        else  if ( line.contains(openingXMLTag) && openingXMLTag.contains("center_name")) {

                            String insertnamespace = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "  + STD_SRA_NAMESPACE + namespace + "\""+ " center_name";
                            line= line.replaceFirst("center_name", insertnamespace)  ;

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
             return new File(filePath + File.separator + originalFile.getName().replaceFirst("_initial.xml", ".xml") );
    }

 public static void delete(File fileName) {
     try {
         String filePath = fileName.getAbsolutePath();

         File target = fileName;
         if (!target.exists()) {
             System.err.println("File " + fileName
                     + " not present to begin with!");
             return;
         }
         if (target.delete())
             System.err.println("** Deleted " + fileName + " **");
         else
             System.err.println("Failed to delete " + fileName);
     } catch (SecurityException e) {
         System.err.println("Unable to delete " + fileName + "("
                 + e.getMessage() + ")");
     }
 }

    public static void main(String[] args) {
        addNameSpaceToFile(new File("/Users/prs/git/ValidatorConverterManager/ISAvalidator-ISAconverter-BIImanager/import_layer/target/export/sra/VS-454-MBL/run_set.xml"),
                "SRA.run.xsd", "<RUN_SET>");
    }


}
