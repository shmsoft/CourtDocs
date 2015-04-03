package com.opr.finshred.pull;

import com.opr.finshred.db.FinShredDB;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author mark
 */
public class JPM_PostPullTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        FinShredDB.createConnection();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FinShredDB.closeConnection();
    }

    /**
     * Test of getResponse method, of class JPM_PostPull.
     */
    @Test
    public void testGetResponse() {
        System.out.println("getResponse");
        JPM_PostPull instance = new JPM_PostPull();
        String result = instance.getResponse();       
        // without the right cookie, the result will not be valid, so let's only check that we got some result
        assertTrue(result.length() > 0);
    }
}