package com.opr.seekingalpha;

import junit.framework.TestCase;

public class SeekingAlphaDownloadTest extends TestCase {

    public void testGetTicker() throws Exception {
        SeekingAlphaDownload instance = new SeekingAlphaDownload();
        assertEquals("AB", instance.getTicker("(AB)"));
        assertEquals("ABC", instance.getTicker("(ABC)"));
        String fragment = "<div class=\"transcript_link\">" +
                "<a href=\"/article/2760615-3m-companys-mmm-management-presents-2015-outlook-meeting-transcript?all=true&find=Earnings%2BCall\">" +
                "3M Company's (MMM) Management Presents 2015 Outlook Meeting - Transcript</a>";
        assertEquals("MMM", instance.getTicker(fragment));
    }
}