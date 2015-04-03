/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opr.finshred.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class FinShredDBTest {
    
    public FinShredDBTest() {
    }
        
    @Before
    public void setUp() {
        FinShredDB.createConnection();
    }
    
    @After
    public void tearDown() {
        FinShredDB.closeConnection();
    }

    /**
     * Test of getNewRun method, of class FinShredDB.
     */
    @Test
    public void testGetNewRun() {
        System.out.println("getNewRun");
        FinShredDB instance = new FinShredDB();
        int result = instance.getNewRun();
        assertTrue(result >= 0);
    }
}