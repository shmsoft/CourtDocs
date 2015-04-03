package com.hyperiongray.ocr;

import java.util.List;

/**
 *
 * Class ImageExtractor.
 *
 * Super class for all image extractors based on their type.
 *
 * @author ilazarov
 *
 */
public abstract class ImageExtractor {


    protected String file;
    protected OCRConfiguration conf;

    protected ImageExtractor(String file) {
        this.file = file;
    }

    /**
     * Extract and return the images for this file, if any
     *
     * @return
     */
    public abstract List<String> extractImages();

    /**
     *
     * Create a image extractor based on the document type.
     *
     * @param type
     * @param file
     * @return
     */
    public static ImageExtractor createImageExtractor(Document.DocumentType type, String file, OCRConfiguration conf) {
        switch (type) {
            case PDF: {
                ImageExtractor imageExtractor = new PDFImageExtractor(file);
                imageExtractor.setConf(conf);

                return imageExtractor;
            }
            default:
                return null;
        }
    }

    public void setConf(OCRConfiguration conf) {
        this.conf = conf;
    }
}
