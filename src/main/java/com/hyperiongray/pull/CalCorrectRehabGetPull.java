package com.hyperiongray.pull;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mark on 4/6/15.
 */
public class CalCorrectRehabGetPull extends GetPull {
    private String startPage;

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
    public List<String> getPdfCollection() throws IOException {
        GetPull getRequest = new GetPull();
        getRequest.setUrl(startPage);
        String pageHtml = getRequest.getResponse();
        // write is just for debugging
        Files.write(pageHtml, new File("test-output/cal.html"), Charset.defaultCharset());
        return getAllPdfLinks(pageHtml);
    }

    public String getStartPage() {
        return startPage;
    }

    public void setStartPage(String startPage) {
        this.startPage = startPage;
    }
    public List<String> getAllPdfLinks(String html) {
        ArrayList<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("[a-zA-Z0-9/]*\\.pdf")
                .matcher(html);
        while (m.find()) {
            list.add(m.group());
        }
        return list;
    }
}