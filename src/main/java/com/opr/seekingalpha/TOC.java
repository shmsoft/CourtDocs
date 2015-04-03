package com.opr.seekingalpha;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class TOC {

    private static final Logger logger = LoggerFactory.getLogger(TOC.class);

    private final Map<String, String[]> toc = new HashMap<>();
    
    private static final int TRANSCRIPT = 17;
    private static final int TICKER = 13;
    private static final int ARTICLE_DATE = 16;

    public void put(String key, String[] value) {
        toc.put(key, value);
    }

    public static TOC readToc(String tableOfContents) throws IOException {
        TOC myToc = new TOC();
        List<String> lines = Files.readLines(new File(tableOfContents), Charset.defaultCharset());
        for (String line : lines) {
            String[] tokens = line.split("\\|");
            myToc.put(tokens[0], tokens);
        }
        logger.info("Read {} lines in the table of contents", lines.size());
        return myToc;
    }

    public boolean isKeyPresent(String key) {
        return toc.containsKey(key);
    }

    public boolean isTranscript(String key) {
        if (!isKeyPresent(key)) {
            return false;
        }
        String[] value = toc.get(key);
        return value[TRANSCRIPT].equals("1");
    }
    
    public String getTicker(String key) {
        if (!isKeyPresent(key)) {
            return "";
        }
        String[] value = toc.get(key);
        return value[TICKER];        
    }
    public String getArticleDate(String key) {
        if (!isKeyPresent(key)) {
            return "";
        }
        String[] value = toc.get(key);
        return value[ARTICLE_DATE];        
    }    
}
