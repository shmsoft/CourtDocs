package com.opr.finshred.pull;

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
public class PostPullTest {
    
    public PostPullTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getResponse method, of class PutPull.
     */
    @Test
    public void testGetResponse() {
        System.out.println("getResponse");
        PostPull instance = new PostPull();
        // TODO find a site to test POST
        instance.setUrl("http://google.com");
        // This would work if you Apache Tomcat is running
        //instance.setUrl("http://localhost:8080/manager/status");
        instance.setPostBody("");
        String expResult = "";
        String result = instance.getResponse();
        assertTrue(result.length() > 0);
    }
}