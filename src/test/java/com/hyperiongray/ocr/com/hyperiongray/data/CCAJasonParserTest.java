package com.hyperiongray.ocr.com.hyperiongray.data;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.google.common.io.Files;
import java.nio.charset.Charset;

public class CCAJasonParserTest extends TestCase {
    @Test
    public void testGetImageUrls() throws IOException {
        CCAJasonParser instance = new CCAJasonParser();
        List<String> jsonLines = Files.readLines(new File("test-data/cca_sample.txt"), Charset.defaultCharset());
        for (String jsonLine: jsonLines) {
            String [] jsonUrls = instance.getImageUrls(jsonLine);
            if (jsonUrls != null) {
                System.out.println(jsonUrls.length + " urls:");
                for (String url: jsonUrls) {
                    System.out.println(url);
                }
            } else {
                System.out.println("No urls");
            }
        }
    }
}