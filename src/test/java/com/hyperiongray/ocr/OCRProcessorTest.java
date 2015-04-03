package com.hyperiongray.ocr;

import junit.framework.TestCase;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class OCRProcessorTest extends TestCase {

    public void testGetImageText() throws IOException {
//        OCRConfiguration conf = new OCRConfiguration();
//        conf.setPdfImageExtractionDir("test-output/ocr/out/");
//        conf.setTesseractWorkDir("test-output/ocr/out/");
//
//        File f = new File("test-output/ocr/out");
//        f.mkdirs();
//
//        OCRProcessor processor = OCRProcessor.createProcessor(conf);
//        List<String> data = processor.getImageText("test-data/N5oOJkR.png");
//        //System.out.println(data.get(0));
//        double match = OCRUtil.compareText(data.get(0), OCRUtil.readFileContent("test-data/N5oOJkR.txt"));
//        assertThat(match, greaterThan(.6));
//
//        data = processor.getImageText("test-data/mUlSeXg.gif");
//        //System.out.println(data.get(0));
//        match = OCRUtil.compareText(data.get(0), OCRUtil.readFileContent("test-data/mUlSeXg.txt"));
//        assertThat(match, greaterThan(.2));  // pretty poor quality!
//
//        data = processor.getImageText("test-data/516.pdf");
//        assertEquals(4, data.size());
//        match = OCRUtil.compareText(data.get(0), OCRUtil.readFileContent("test-data/516.txt"));
//        assertThat(match, greaterThan(.6));
//
//        System.out.println("516.pdf = Words matching: " + match);
//
//        data = processor.getImageText("test-data/testb.pdf");
//
//        assertThat(data.size(), equalTo(2));
//        double match1 = OCRUtil.compareText(data.get(0), OCRUtil.readFileContent("test-data/testb_1.txt"));
//        double match2 = OCRUtil.compareText(data.get(1), OCRUtil.readFileContent("test-data/testb_2.txt"));
//        assertThat(match1, greaterThan(.9));
//        assertThat(match2, greaterThan(.75));
    }
}