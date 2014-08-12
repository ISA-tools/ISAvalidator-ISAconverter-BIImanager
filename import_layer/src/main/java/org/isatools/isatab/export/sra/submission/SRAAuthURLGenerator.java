package org.isatools.isatab.export.sra.submission;

import java.net.URLEncoder;
import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import uk.ac.ebi.embl.era.rest.common.Base64;

/**
 * This class is used to generate the digisted/ authentication
 * URL which can be used by programmatic submitter to submit
 * SRA data and its metadata
 * More about it can be found here
 * https://www.ebi.ac.uk/ena/about/training/sra_rest_tutorial#part4
 * @author Rajesh Radhakrishnan (rajeshr@ebi.ac.uk)
 * @author agbeltran
 *
 */
public class SRAAuthURLGenerator {

    private static SRAAuthURLGenerator instance;
    static Logger logger = Logger.getLogger(SRAAuthURLGenerator.class.getName( ));
    Appender appender;
    PatternLayout layout;

    /**
     *
     */
    public SRAAuthURLGenerator() {
        logger.setLevel(Level.INFO);
        String pattern =  "";
        pattern += "Date : %d{ISO8601} %n";
        pattern += "%l %n";
        pattern += "%m";
        layout = new PatternLayout(pattern);
        try {
            appender = new ConsoleAppender(layout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.addAppender(appender);
    }

    public static synchronized SRAAuthURLGenerator getInstance() {
        if (instance == null) {
            instance = new SRAAuthURLGenerator();
        }
        return instance;
    }

    public byte[] calculateRFC2104HMAC(String data, String key) throws java.security.SignatureException {
        try {

            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            return rawHmac;

        } catch (Exception e) {
            throw new SignatureException(" Failed to generate HMAC : " + e.getMessage());
        }
    }

    /**
     * This method creates an encrypted string made out of path and password
     *
     * @param path, String
     * @param password, password of the drop box
     * @return String encrypted String
     * @throws Exception
     */
    private String getSha1(String path, String password) throws Exception {
        String encrypted = null;
        byte[] sums = calculateRFC2104HMAC(path, password);
        encrypted = Base64.byteArrayToBase64(sums);
        return encrypted;
    }

    public String getAuthenticatedURL(ENARestServer server, String user, String password) throws Exception {
        String sha1Signature = getSha1(ENARestServer.PATH, password);

        String authStringBeforeEncoding = "ERA " + user + " " + sha1Signature;
        String authStringAfterEncoding = "auth="
                + URLEncoder.encode(authStringBeforeEncoding, "UTF-8").replace("+", "%20");

        String finalDevAuthenicationURL = null;

        switch (server) {
            case DEV:
                finalDevAuthenicationURL = ENARestServer.DEV.getURL() + ENARestServer.PATH + "?" + authStringAfterEncoding;
                break;
            case TEST:
                finalDevAuthenicationURL = ENARestServer.TEST.getURL() + ENARestServer.PATH + "?" + authStringAfterEncoding;
                break;
            case PROD:
                finalDevAuthenicationURL = ENARestServer.PROD.getURL() + ENARestServer.PATH + "?" + authStringAfterEncoding;
                break;
            default:
                finalDevAuthenicationURL = "";

        }
        return finalDevAuthenicationURL;
    }


    /**
     * Please provide user name which is the ena drop box and its password as the input parameters
     * @param args
     */
    public static void main(String[] args) {
        boolean success = false;
        try {
            String usage = "Usage :SRAAuthenticationURLGenerator <user> <password>";
            if (args.length != 2) {
                System.err.println(usage);
                throw new Exception(usage);
            }
            String user = args[0];
            String password =args[1];
            logger.setLevel(Level.ERROR);

            SRAAuthURLGenerator eac = new SRAAuthURLGenerator();

            System.out.println(eac.getAuthenticatedURL(ENARestServer.DEV,user, password));

            System.out.println(eac.getAuthenticatedURL(ENARestServer.TEST,user, password));

            System.out.println(eac.getAuthenticatedURL(ENARestServer.PROD, user, password));


        }  catch (Exception e) {
            if (e.getMessage() != null) {
                System.err.println("Error Message:" + e.getMessage());
            }
            StackTraceElement elements[] = e.getStackTrace();
            for (StackTraceElement element : elements) {
                System.err.println(element.getFileName() + ":"
                        + element.getLineNumber() + " >> "
                        + element.getMethodName() + "()");

            }
            System.exit(1);
        }
    }
}