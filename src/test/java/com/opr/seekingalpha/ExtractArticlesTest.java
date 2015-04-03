package com.opr.seekingalpha;

import com.opr.finshred.Settings;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mark
 */
public class ExtractArticlesTest {

    public ExtractArticlesTest() {
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
     * Test of main method, of class ExtractArticles.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMain() throws Exception {
        String[] argv = new String[3];
        if (Settings.getOs() != Settings.OS.WINDOWS) {
            argv[0] = "testdata/seekingalpha/it_articles_new.head.csv";
            argv[1] = "testdata/seekingalpha/sa_articles_new.head.csv";
            argv[2] = "output/seekingalpha/extracted_articles";
            FileUtils.deleteDirectory(new File(argv[2]));
            ExtractArticles.main(argv);
        }
    }
}
