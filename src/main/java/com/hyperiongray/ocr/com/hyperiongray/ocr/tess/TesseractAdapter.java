package com.hyperiongray.ocr.com.hyperiongray.ocr.tess;

import java.util.List;

import com.hyperiongray.ocr.com.hyperiongray.util.PlatformUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class TesseractAdapter.
 *
 * Call OS specific command for tesseract.
 *
 */
public class TesseractAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TesseractAdapter.class);
    private static final String TESSERACT_VERSION_LINE = "tesseract 3.";
    private static TesseractAdapter __instance;
    private String tesseractBin;

    private TesseractAdapter(String tesseractBin) {
        this.tesseractBin = tesseractBin;
    }

    public static synchronized TesseractAdapter createTesseract(String tesseractBin) {
        if (__instance == null) {
            __instance = new TesseractAdapter(tesseractBin);
        }

        return __instance;
    }

    /**
     * Call the tesseract bin to extract the image file text.
     *
     * @param imageFile
     * @param outputPath
     * @return
     */
    public List<String> call(String imageFile, String outputPath) {
        String[] cmd = {tesseractBin, imageFile, outputPath};
        return PlatformUtil.runUnixCommand(cmd, false);
    }

    /**
     *
     * Verify that the tesseract application is installed and has a proper version.
     *
     * @return true if tesseract is installed, false if not.
     */
    public boolean verifyTesseract() {
        if (PlatformUtil.isNix()) {
            List<String> output = PlatformUtil.runCommand(tesseractBin + " -v", true);
            for (String line : output) {
                if (line.startsWith(TESSERACT_VERSION_LINE)) {
                    logger.info("Tesseract installation verified");
                    return true;
                }
            }
        }
        logger.error("Tesseract is not installed, but it is required");
        return false;
    }

    private static String escapeImageName(String imageName) {
        return imageName.replace(" ", "\\ ").replace("!", "\\!");
    }
}
