package com.hyperiongray.court;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mark on 5/20/15.
 */
public class NYAppealParseTest {
    private static final Logger logger = LoggerFactory.getLogger(NYAppealParseTest.class);

    //@Test
    public void testRegex() throws IOException {
        System.out.println("testRegex");
        NYAppealParse instance = new NYAppealParse();
        File[] files = new File("test-data/ny_appeals").listFiles();
        Arrays.sort(files);
        for (File file: files) {
            String text= FileUtils.readFileToString(file);
            Map<String, String> answer = instance.extractInfo(file);
            for (int e = 0; e < NYAppealParse.KEYS.values().length; ++e) {
                String key = NYAppealParse.KEYS.values()[e].toString();
                if (answer.containsKey(key)) {
                    System.out.println(key + ": " + answer.get(key));
                }
            }
        }
    }
    @Test
    public void testCompleteParse() throws IOException {
        System.out.println("testCompleteParse");
        String [] args = {"-i", "test-data/ny_appeals", "-o", "test-output/parse"};
        NYAppealParse.main(args);
    }
}