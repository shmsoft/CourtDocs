package com.opr.xdb;

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
public class XdbMetaTest {
    
    public XdbMetaTest() {
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
     * Test of main method, of class XdbMeta.
     */
    // @Test
    public void testMain() {
        String[] argv = new String[3];
        argv[0] = "-i testdata/DAYFACTOR_SAMPLE.txt";
        argv[0] = "-i testdata/RS_Metrics_Merged_CSV_20141024.csv";        
        argv[1] = "-o output/xdb_meta.xml";
        argv[2] = "-t";
        XdbMeta.main(argv);
    }    
}
