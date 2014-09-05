package org.isatools.isatab.export.sra.submission;

/**
 * Created by agbeltran on 04/09/2014.
 */
public class ENAResponse {

    private int status_code;
    private String receipt;

    public ENAResponse(int code, String receipt){
        this.status_code = code;
        this.receipt = receipt;
    }

    public int getStatus_code(){
        return status_code;
    }

    public String getReceipt(){
        return receipt;
    }


}
