package com.opr.seekingalpha;

import com.opr.finshred.pull.GetPull;

/**
 *
 * @author mark
 */
public class SeekingAlphaGetPull extends GetPull {
    @Override
    public String getResponse() {
        setHeaders();
        return super.getResponse();
    }

    private void setHeaders() {
//        setUrl("http://seekingalpha.com/search/transcripts?term=quarterly&page=2");
//        addHeader("Host", "www.capitaliq.com");
//        addHeader("Connection", "keep-alive");
//        addHeader("Accept", "text/html, application/xhtml+xml, */*");
//        addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//        addHeader("Accept-Language", "en-US");
//        addHeader("Accept-Encoding", "gzip, deflate");;
    }
}
