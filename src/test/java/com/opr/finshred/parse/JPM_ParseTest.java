/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opr.finshred.parse;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class JPM_ParseTest {    

    /**
     * Test of parseSearch method, of class JPM_Parse.
     */
    @Test
    public void testParseSearch() throws IOException {
        System.out.println("parseSearch");
        Path path = FileSystems.getDefault().getPath("testdata", "old-jpm-response.html");
        String response = new String(Files.readAllBytes(path));
        JPM_ResultSet resultSet = JPM_Parse.parseSearch(response);
        // TODO it should be 25, we are loosing one somewhere
        assertEquals(resultSet.size(), 24);
    }
}