package com.hyperiongray.ocr.com.hyperiongray.ocr.tess;

import com.hyperiongray.ocr.OCRConfiguration;

public class TesseractOCRFactory {

    //keep a single object
    private static TesseractOCR tesseractOCR;

    /**
     *
     * Create a TesseractOCR instance.
     *
     * @param conf
     * @return
     */
    public static synchronized TesseractOCR createTesseractOCR(OCRConfiguration conf) {
        if (tesseractOCR == null) {
            TesseractAdapter tessAdapter = TesseractAdapter.createTesseract(conf.getTesseractBin());
            tesseractOCR = new TesseractOCR();
            tesseractOCR.setConfiguration(conf);
            tesseractOCR.setTessAdapter(tessAdapter);
        }

        return tesseractOCR;
    }
}
