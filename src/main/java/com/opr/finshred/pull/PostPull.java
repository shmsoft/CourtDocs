package com.opr.finshred.pull;

import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pull the contents resulting from the HTTP PUT request.
 *
 * @author mark.
 */
public class PostPull extends Pull {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public String getResponse() {
        // TODO  convert to try-with-resources
        String result = "";
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {

            HttpPost httpPost = new HttpPost(getUrl());
            // put in all custom headers
            Map<String, String> headers = getHeaders();
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPost.addHeader(header.getKey(), header.getValue());
            }
            HttpEntity entity = new ByteArrayEntity(getPostBody().getBytes("UTF-8"));
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            logger.debug("Status line: {}", response.getStatusLine().toString());
            HttpEntity responseEntity = response.getEntity();
            result = EntityUtils.toString(responseEntity);
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(responseEntity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    // nothing to fix in that case
                }
            }
        }
        return result;
    }
}
