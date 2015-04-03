package com.opr.finshred.pull;

import com.opr.finshred.db.FinShredDB;

/**
 *
 * @author mark
 */
public class SeekingAlphaPostPull extends PostPull {

    @Override
    public String getResponse() {
        setHeadersAndBody();
        return super.getResponse();
    }
    private void setHeadersAndBody() {
        setUrl("http://seekingalpha.com/authentication/login");
        addHeader("Host", "seekingalpha.com");
        addHeader("Accept", "text/html, application/xhtml+xml, */*");        
        addHeader("Accept-Language", "en-US");
        addHeader("Accept-Encoding", "gzip, deflate");
        addHeader("Connection", "keep-alive");
        addHeader("Content-Type", "application/x-www-form-urlencoded");  
        String postBody = "id=headtabs_login&activity=footer_login&function=FooterBar.Login&user%5Bemail%5D=mark%40elephantscale.com&user%5Bpassword%5D=lovelena";
        setPostBody(postBody);
    }
    public static void main(String [] args) {
        SeekingAlphaPostPull instance = new SeekingAlphaPostPull();
        String response = instance.getResponse();
    }
}
