package com.opr.finshred;

import com.opr.s3.S3Agent;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public void moveDownloadedFiles(String ticker, Settings.SITES site, String downloadTimeStamp) {
        Settings settings = Settings.getSettings();
        File[] files = new File(
                settings.getFirefoxDownloadDir()).listFiles();
        String moveTo = settings.getMyDownloadDir() + File.separator 
                + downloadTimeStamp + File.separator
                + site.toString() + File.separator                
                + getTickerDir(ticker);
        try {
            File moveToFile = new File(moveTo);
            moveToFile.mkdirs();
            FileUtils.cleanDirectory(new File(moveTo));
            for (File file : files) {
                FileUtils.moveFileToDirectory(file, new File(moveTo), true);
                if (settings.isUploadToS3()) {
                    uploadFileToS3(moveTo + File.separator + file.getName());
                }
            }
        } catch (IOException e) {
            logger.error("Problem moving downloaded files", e);
        }

    }

    private String getTickerDir(String ticker) {
        if (ticker.indexOf(":") > 0) {
            String[] parts = ticker.split(":");
            return parts[1];
        } else {
            return ticker;
        }
    }

    private void uploadFileToS3(String filePath) {
        S3Agent agent = new S3Agent();
        agent.putFileInS3(filePath, filePath);
    }

    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            // nothing there
        }
    }

    public static void cleanupDownloadDir() {
        String downloadDir = Settings.getSettings().getFirefoxDownloadDir();
        logger.debug("Cleaning out the Firefox download directory {}", downloadDir);
        try {
            FileUtils.cleanDirectory(new File(downloadDir));
        } catch (IOException e) {
            logger.error("Problem cleaning up Firefox download dir", e);
        }
    }
}
