package com.opr.finshred.parse;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class ParseUtilTest {

    /**
     * Test of getAllBetweenDelimiters method, of class ParseUtil.
     */
    @Test
    public void testGetAllBetweenDelimiters() throws IOException {
        System.out.println("getAllBetweenDelimiters");
        Path path = FileSystems.getDefault().getPath("testdata", "capitalIQ-response.html");
        String text = new String(Files.readAllBytes(path));
        String del1 = "<a href=\"/CIQDotNet/Transcripts/Detail.aspx?keyDevId=";
        String del2 = "\"";
        List result = ParseUtil.getAllBetweenDelimiters(text, del1, del2);
        assertEquals(result.size(), 25);
    }

    /**
     * Test of parseSearch method, of class JPM_Parse.
     */
    @Test
    public void testGetBetweenDelimiters() throws IOException {
        System.out.println("getBetweenDelimiters");
        String testStr = "abc>def<geh";
        String extracted = ParseUtil.getBetweenDelimiters(testStr, ">", "<", 0);
        assertEquals(extracted, "def");
        extracted = ParseUtil.getBetweenDelimiters(testStr, ">", "<", 6);
        assertEquals(extracted, "");
    }
}