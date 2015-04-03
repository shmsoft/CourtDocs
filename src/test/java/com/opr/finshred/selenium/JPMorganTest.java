package com.opr.finshred.selenium;

import com.opr.finshred.Settings;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class JPMorganTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        Settings settings = Settings.getSettings();
        String[] tickers = {"AAPL.O", "GOOG.O"};
        settings.setTickers(tickers);
    }

    /**
     * Test of run method, of class JPMorgan.
     */
    //@Test
    public void testRun() {
        JPMorgan instance = new JPMorgan();
        instance.run();
    }

    @Test
    public void testFindDocIds() throws IOException {
        JPMorgan instance = new JPMorgan();
        Path path = FileSystems.getDefault().getPath("testdata", "jpm-page.html");
        String html = new String(Files.readAllBytes(path));
        List<String> result = instance.findDocIds(html);
        assertEquals(result.size(), 4);
    }
}