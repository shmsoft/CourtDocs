package com.opr.finshred.pull;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Pull the contents resulting from the HTTP PUT request.
 *
 * @author mark
 */
public class GetPull extends Pull {

    @Override
    public String getResponse() {
        String result = "";
        // TODO  convert to try-with-resources
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(getUrl());
            response = httpclient.execute(httpGet);
            // The underlying HTTP connection is still held by the response object
            // to allow the response content to be streamed directly from the network socket.
            // In order to ensure correct deallocation of system resources
            // the user MUST either fully consume the response content  or abort request
            // execution by calling CloseableHttpResponse#close().
            HttpEntity entity1 = response.getEntity();
            HttpEntity responseEntity = response.getEntity();
            result = EntityUtils.toString(responseEntity);
            EntityUtils.consume(entity1);
        } catch (IOException e) {
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    // nothing doing, swallow the exception
                }
            }
        }
        return result;
    }
}
