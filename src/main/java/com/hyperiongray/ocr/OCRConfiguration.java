package com.hyperiongray.ocr;

import org.apache.hadoop.mapreduce.Mapper.Context;

public class OCRConfiguration {

    private static final String TESS_BIN = "tesseract";
    private static final String TESS_WORK_DIR = "/tmp" + "/tesseract/";
    private static final String PDF_IMAGE_EXTRACTION_DIR = "/tmp" + "tesseract/";
    private static final String TESS_OUT_EXT = "txt";
    private String tesseractBin;
    private String tesseractWorkDir;
    private String tesseractOutputExtension;
    private String pdfImageExtractionDir;
    private Context context;

    public OCRConfiguration() {
        this(TESS_BIN, TESS_WORK_DIR, TESS_OUT_EXT, PDF_IMAGE_EXTRACTION_DIR, null);
    }

    public OCRConfiguration(String tesseractWorkDir, Context context) {
        this(TESS_BIN, tesseractWorkDir, TESS_OUT_EXT, tesseractWorkDir, context);
    }

    public OCRConfiguration(String tesseractBin, String tesseractWorkDir,
                            String tesseractOutputExtension, String pdfImageExtractionDir, Context context) {
        this.tesseractBin = tesseractBin;
        this.tesseractWorkDir = tesseractWorkDir + "/";
        this.tesseractOutputExtension = tesseractOutputExtension;
        this.pdfImageExtractionDir = pdfImageExtractionDir + "/";
        this.context = context;
    }

    public String getTesseractBin() {
        return tesseractBin;
    }

    public void setTesseractBin(String tesseractBin) {
        this.tesseractBin = tesseractBin;
    }

    public String getTesseractWorkDir() {
        return tesseractWorkDir;
    }

    public void setTesseractWorkDir(String tesseractWorkDir) {
        this.tesseractWorkDir = tesseractWorkDir;
    }

    public String getTesseractOutputExtension() {
        return tesseractOutputExtension;
    }

    public void setTesseractOutputExtension(String tesseractOutputExtension) {
        this.tesseractOutputExtension = tesseractOutputExtension;
    }

    public String getPdfImageExtractionDir() {
        return pdfImageExtractionDir;
    }

    public void setPdfImageExtractionDir(String pdfImageExtractionDir) {
        this.pdfImageExtractionDir = pdfImageExtractionDir;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
