package com.hyperiongray.court;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by mark on 5/20/15.
 */
public class NYAppealParseTest {
    private static final Logger logger = LoggerFactory.getLogger(NYAppealParseTest.class);

    @Test
    public void testConnect() throws IOException {
        NYAppealParse instance = new NYAppealParse();
        System.out.println(instance.toString());
    }
}