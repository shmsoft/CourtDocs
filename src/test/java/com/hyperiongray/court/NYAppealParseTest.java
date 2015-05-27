package com.hyperiongray.court;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by mark on 5/20/15.
 */
public class NYAppealParseTest {
    private static final Logger logger = LoggerFactory.getLogger(NYAppealParseTest.class);

    @Test
    public void testRegex() throws IOException {
        System.out.println("testRegex");
        NYAppealParse instance = new NYAppealParse();
        File[] files = new File("test-data/ny_appeals").listFiles();
        Arrays.sort(files);
        for (File file: files) {
            System.out.println("File: " + file.getName());
            String text= FileUtils.readFileToString(file);
            Map<String, String> answer = instance.extractInfo(text);
            for (int e = 0; e < NYAppealParse.extracts.length; ++e) {
                String key = NYAppealParse.extracts[e][0];
                if (answer.containsKey(key)) {
                    System.out.println(key + ": " + answer.get(key));
                }
            }
        }
    }
}