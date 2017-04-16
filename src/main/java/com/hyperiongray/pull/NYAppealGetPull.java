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
public class NYAppealGetPull extends GetPull {
    private String startPage;

    @Override
    public String getResponse() {
        return super.getResponse();
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