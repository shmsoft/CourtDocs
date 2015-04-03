package com.opr.s3;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class S3AgentTest {

    /**
     * Test of putFileInS3 method, of class S3Agent.
     */
    @Test
    public void testPutFileInS3() {
        System.out.println("putFileInS3");
        String key = "test-upload-safe-to-delete";
        String fileName = "conf/CapitalIQ-tickers.txt";
        S3Agent instance = new S3Agent();
        instance.setContentType("text/text");
        boolean result = instance.putFileInS3(key, fileName);
        assertTrue(result);
    }

    //@Test
    // this is tested by other functionality anyway
    public void getFileFromS3AsString() {
        S3Agent instance = new S3Agent();
        String bucket = "opr-solr-data";
        String key = "earnings_transcripts.json";
        instance.setMyBucket(bucket);
        String fileContents = instance.getFileFromS3AsString(key);
        assertEquals("test-data", fileContents);
    }
    /**
     * Test of putFileInS3 method, of class S3Agent.
     * @throws java.io.IOException
     */
    //@Test
    public void testMain() throws IOException {
        // prepare test data by copying
        File testDataDir = new File("target/upload");
        testDataDir.mkdirs();
        for (File file: new File("testdata/upload").listFiles()) {
            Files.copy(file, new File(testDataDir.getPath() + File.separator + file.getName()));
        }
        String argv[] = new String[5];
        argv[0] = "hedge-iq";
        argv[1] = "AKIAJSGS7DOH6GBCQJAA";
        argv[2] = "n+H0ap8ZGLR/DTSGJD1XyXO8nzKKf9i7dDIM5gkS";        
        argv[3] = testDataDir.getPath();
        argv[4] = "uploads";
        S3Agent.main(argv);
    }
}