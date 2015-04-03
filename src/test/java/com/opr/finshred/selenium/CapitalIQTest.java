package com.opr.finshred.selenium;

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
public class CapitalIQTest {

    /**
     * Test of main method, of class CapitalIQ.
     */
    @Test
    public void testfindPdfIds() throws Exception {
        System.out.println("findPdfId");
        CapitalIQ instance = new CapitalIQ();
        Path path = FileSystems.getDefault().getPath(
                "testdata", "CapIQ-search-page-source.html");        
        String html = new String(Files.readAllBytes(path));
        List <String> ids = instance.findPdfIds(html);
        assertEquals(ids.size(), 22);
    }
}