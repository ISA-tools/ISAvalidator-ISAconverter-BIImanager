package org.isatools.isatab.export.sra.submission;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Created by agbeltran
 */
public class SRASubmitter {

    // curl -F "SUBMISSION=@submission.xml" -F "STUDY=@study.xml" -F "SAMPLE=@sample_set.xml" -F "EXPERIMENT=@experiment_set.xml" -F "RUN=@run_set.xml" <url> --insecure

    private static final String SUBMISSION_FILE = "submission.xml";
    private static final String STUDY_FILE = "study.xml";
    private static final String SAMPLE_FILE = "sample_set.xml";
    private static final String EXPERIMENT_FILE = "experiment_set.xml";
    private static final String RUN_FILE = "run_set.xml";

    private SRAAuthURLGenerator urlGenerator = new SRAAuthURLGenerator();

    public SRASubmitter() {

    }

    public ENAResponse submit(ENARestServer server, String user, String password, String folder_path) {
        try {
            String authURL = urlGenerator.getAuthenticatedURL(server, user, password);

            Client client = null;
            if (server.equals(ENARestServer.TEST)) {
                HostnameVerifier hv = getHostnameVerifier();
                ClientConfig config = new DefaultClientConfig();
                SSLContext ctx = this.getSSLContext();
                config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                        new HTTPSProperties(hv, ctx));

                client = Client.create(config);
            } else {
                client = Client.create();
            }

            WebResource webResource = client.resource(authURL);

            InputStream in_submission = new FileInputStream(folder_path + SUBMISSION_FILE);
            InputStream in_study = new FileInputStream(folder_path + STUDY_FILE);
            InputStream in_sample = new FileInputStream(folder_path + SAMPLE_FILE);
            InputStream in_experiment = new FileInputStream(folder_path + EXPERIMENT_FILE);
            InputStream in_run = new FileInputStream(folder_path + RUN_FILE);

            FormDataMultiPart fdmp = new FormDataMultiPart();

            //submission
            final FormDataContentDisposition disposition_submission = FormDataContentDisposition//
                    .name("SUBMISSION")//
                    .fileName(folder_path + SUBMISSION_FILE)//
                    .build();

            FormDataBodyPart fdp_submission = new FormDataBodyPart(
                    disposition_submission,
                    in_submission,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE);

            fdmp.bodyPart(fdp_submission);

            //study
            final FormDataContentDisposition disposition_study = FormDataContentDisposition//
                    .name("STUDY")//
                    .fileName(folder_path + STUDY_FILE)//
                    .build();

            FormDataBodyPart fdp_study = new FormDataBodyPart(
                    disposition_study,
                    in_study,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE);

            fdmp.bodyPart(fdp_study);


            //sample
            final FormDataContentDisposition disposition_sample = FormDataContentDisposition//
                    .name("SAMPLE")//
                    .fileName(folder_path + SAMPLE_FILE)//
                    .build();

            FormDataBodyPart fdp_sample = new FormDataBodyPart(
                    disposition_sample,
                    in_sample,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE);

            fdmp.bodyPart(fdp_sample);

            //experiment
            final FormDataContentDisposition disposition_experiment = FormDataContentDisposition//
                    .name("EXPERIMENT")//
                    .fileName(folder_path + EXPERIMENT_FILE)//
                    .build();

            FormDataBodyPart fdp_experiment = new FormDataBodyPart(
                    disposition_experiment,
                    in_experiment,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE);

            fdmp.bodyPart(fdp_experiment);


            //run
            final FormDataContentDisposition disposition_run = FormDataContentDisposition//
                    .name("RUN")//
                    .fileName(folder_path + RUN_FILE)//
                    .build();

            FormDataBodyPart fdp_run = new FormDataBodyPart(
                    disposition_run,
                    in_run,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE);

            fdmp.bodyPart(fdp_run);


            ClientResponse clientResponse = webResource.type(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_XML)
                    .post(ClientResponse.class, fdmp);

            System.out.println("ENASubmitter - client response status = "+ clientResponse.getStatusInfo().getStatusCode());

            ENAResponse response = new ENAResponse(clientResponse.getStatusInfo().getStatusCode(), clientResponse.getEntity(String.class));

            return response;

        } catch (Exception ex) {
            System.err.println("error!");
            ex.printStackTrace();
        }
        return null;
    }

    private SSLContext getSSLContext() throws NoSuchAlgorithmException,
            KeyManagementException {
        final SSLContext sslContext = SSLContext.getInstance("SSL");

        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }


            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }
        }}, new SecureRandom());

        return sslContext;
    }

    private HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hv = new HostnameVerifier() {

            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };

        return hv;
    }
}