package com.hyperiongray.court;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.hyperiongray.court.parser.DataKey;
import com.hyperiongray.court.parser.TextParser;

/**
 * Created by mark on 5/20/15.
 */
public class NYAppealParseTest {

    //@Test
    public void testRegex() throws IOException {
        System.out.println("testRegex");
        TextParser parser = new TextParser();
        File[] files = new File("test-data/ny_appeals").listFiles();
        Arrays.sort(files);
        for (File file: files) {
            String text = FileUtils.readFileToString(file);
            Map<DataKey, String> answer = parser.parseFile(file);
            for (int e = 0; e < DataKey.values().length; ++e) {
                String key = DataKey.values()[e].toString();
                if (answer.containsKey(key)) {
                    System.out.println(key + ": " + answer.get(key));
                }
            }
        }
    }
    
    @Test
    public void testCompleteParse() throws IOException {
        System.out.println("testCompleteParse");
        String [] args = {"-i", "test-data/ny_appeals", "-o", "test-output/parse", "-parser", "gate"};
        Application.main(args);
    }
    
}