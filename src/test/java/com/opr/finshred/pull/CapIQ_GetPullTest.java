package com.opr.finshred.pull;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class CapIQ_GetPullTest {
    
    /**
     * Test of getResponse method, of class CapIQ_GetPull.
     */
    @Test
    public void testGetResponse() {
        CapIQ_GetPull instance = new CapIQ_GetPull();
        String result = instance.getResponse();
        assertTrue(result.length() > 0);
    }
}