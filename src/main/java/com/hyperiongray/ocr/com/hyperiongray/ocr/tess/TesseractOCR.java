package com.hyperiongray.ocr.com.hyperiongray.ocr.tess;

import java.io.File;


import com.hyperiongray.ocr.OCRConfiguration;
import com.hyperiongray.ocr.OCREngine;
import com.hyperiongray.ocr.OCRUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class TesseractOCR.
 *
 */
public class TesseractOCR implements OCREngine {
    private static final Logger logger = LoggerFactory.getLogger(TesseractOCR.class);
    private Boolean tesseractAvailable;
    private TesseractAdapter tessAdapter;
    private OCRConfiguration configuration;

    protected TesseractOCR() {
        //hiding the constructor. Get and instance using the factory.
    }

    @Override
    public String getImageText(String imageFile) {
        try {
            File outDir = new File(configuration.getTesseractWorkDir());
            outDir.mkdirs();

            String output = configuration.getTesseractWorkDir() + OCRUtil.createUniqueFileName("out");
            tessAdapter.call(imageFile, output);

            String outputFile = output + "."
                    + configuration.getTesseractOutputExtension();

            logger.trace("TesseractOCR - processing, outputFile: {} waiting...", outputFile);

            File resultFile = new File(outputFile);
            // as the creation of the file goes in OS background, wait for the result
            int maxRetries = 6;
            while (!resultFile.exists() && maxRetries > 0) {
                // sleep 1 second
                try {
                    Thread.sleep(500);
                    maxRetries--;
                } catch (InterruptedException e) {
                    logger.trace("Interrupted: ", e);
                }
            }

            if (!resultFile.exists()) {
                //logger.warn("TesseractOCR - image file not recognized");
                return null;
            }

            logger.trace("TesseractOCR - file processed: {}", outputFile);

            return OCRUtil.readFileContent(outputFile);
        } catch (Exception e) {
            logger.error("TesseractOCR - Problem processing image: {}", imageFile, e);
        }

        return null;
    }

    @Override
    public boolean isEngineAvailable() {
        synchronized (this) {
            if (tesseractAvailable == null) {
                tesseractAvailable = tessAdapter.verifyTesseract();
            }

            return tesseractAvailable;
        }
    }

    public void setTessAdapter(TesseractAdapter tessAdapter) {
        this.tessAdapter = tessAdapter;
    }

    public void setConfiguration(OCRConfiguration configuration) {
        this.configuration = configuration;
    }
}
