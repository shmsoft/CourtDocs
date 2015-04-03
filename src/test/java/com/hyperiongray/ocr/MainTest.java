package com.hyperiongray.ocr;

import static org.junit.Assert.*;

public class MainTest {


    @org.junit.Test
    public void testMainZipThrough() throws Exception {
        String args[] = new String[3];
        args[0] = "-f";
        args[1] = "test-data/cca_sample.txt";
        args[2] = "-z";
        Main.main(args);
    }

    @org.junit.Test
    public void testMainWithOCR() throws Exception {
        String args[] = new String[4];
        args[0] = "-f";
        args[1] = "test-data/cca_sample.txt";
        args[2] = "-o";
        args[3] = "test-output/ocr-d.json";
        Main.main(args);
    }
}