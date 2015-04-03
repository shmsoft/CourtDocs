package com.hyperiongray.ocr;

public interface OCREngine {

    /**
     * Do OCR processing over the given image file
     * and return the recognized text.
     *
     * @param imageFile
     * @return
     */
    String getImageText(String imageFile);

    /**
     * Check if the OCR engine is available.
     * This should encapsulate all platform specific
     * checks.
     */
    boolean isEngineAvailable();
}
