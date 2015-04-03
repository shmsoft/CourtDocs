package com.opr.finshred;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collect all download settings throughout the run.
 *
 * @author mark
 */
public class Settings {

    public static final String VERSION = "2.0.2";

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public enum SITES {

        CapitalIQ, JPMorgan, Zacks
    };
    private SITES site;
    private Date start;
    private Date end;
    private int totalLimit;
    private int perCompanyLimit;
    private String[] tickers;
    private int[] selectedTickers = new int[0];
    private final static String firefoxDownloadDirLinux = "/home/mark/Downloads";
    private final static String firefoxDownloadDirWindows = "C:\\Users\\Administrator\\Downloads";
    private String firefoxDownloadDir;
    private String myDownloadDir = "finshred-downloads";
    private String capIqUsername = "";
    private String capIqPassword = "";
    private String amazonId = "";
    private String amazonKey = "";
    private String bucket = "";
    private boolean uploadToS3 = true;
    private int earliestYear = 2005;
    private int emptyPagesToStop = 3;

    private String tickerFile = "data/test-tickers.txt";
    private int maxPages;
    /**
     * @return the capIqUsername
     */
    public String getCapIqUsername() {
        return capIqUsername;
    }

    /**
     * @param capIqUsername the capIqUsername to set
     */
    public void setCapIqUsername(String capIqUsername) {
        this.capIqUsername = capIqUsername;
    }

    /**
     * @return the capIqPassword
     */
    public String getCapIqPassword() {
        return capIqPassword;
    }

    /**
     * @param capIqPassword the capIqPassword to set
     */
    public void setCapIqPassword(String capIqPassword) {
        this.capIqPassword = capIqPassword;
    }

    /**
     * @return the amazonId
     */
    public String getAmazonId() {
        return amazonId;
    }

    /**
     * @return the amazonKey
     */
    public String getAmazonKey() {
        return amazonKey;
    }

    /**
     * @return the bucket
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * @return the uploadToS3
     */
    public boolean isUploadToS3() {
        return uploadToS3;
    }

    /**
     * @param uploadToS3 the uploadToS3 to set
     */
    public void setUploadToS3(boolean uploadToS3) {
        this.uploadToS3 = uploadToS3;
    }

    private enum FORMAT {

        PDF, HTML
    };
    private FORMAT format;
    private static final Settings settings = new Settings();
    private static final Logger logger = LoggerFactory.getLogger("Settings");

    private Settings() {
        // singleton
        setSiteAndReadTickers(SITES.CapitalIQ.toString());
        readCredentials();
        // note that at the moment Mac default to Linux
        // this will need to be changed for convenience if used on Mac
        if (getOs() == OS.WINDOWS) {
            firefoxDownloadDir = firefoxDownloadDirWindows;
        } else {
            firefoxDownloadDir = firefoxDownloadDirLinux;
        }

    }

    public static Settings getSettings() {
        return settings;
    }

    /**
     * @return the site
     */
    public String getSite() {
        return site.toString();
    }

    public boolean isCapIQ() {
        return site == SITES.CapitalIQ;
    }

    public boolean isJPM() {
        return site == SITES.JPMorgan;
    }
    
     public boolean isZacks() {
        return site == SITES.Zacks;
    }

    /**
     * @param site the site to set
     */
    public final void setSiteAndReadTickers(String site) {
        if (site.equalsIgnoreCase(SITES.CapitalIQ.toString())) {
            this.site = SITES.CapitalIQ;
        } else if (site.equalsIgnoreCase(SITES.JPMorgan.toString())) {
            this.site = SITES.JPMorgan;
        } else if (site.equalsIgnoreCase(SITES.Zacks.toString())) {
            this.site = SITES.Zacks;
        }
        readTickers();
    }

    private void readTickers() {
        if (site == SITES.CapitalIQ) {
            readCapitalIqTickers();
        } else if (site == SITES.JPMorgan) {
            readJPMorganTickers();
        } else if (site == SITES.Zacks) {
            readZacksSectors();
        }
    }

    private void readCapitalIqTickers() {
        tickers = new String[0];
        try {
            tickers = FileUtils.readLines(new File("conf/CapitalIQ-tickers.txt")).toArray((new String[0]));
        } catch (IOException e) {
            logger.error("Problem reading tickers in config", e);
        }
    }

    private void readJPMorganTickers() {
        tickers = new String[0];
        try {
            tickers = FileUtils.readLines(new File("conf/JPM-tickers.txt")).toArray((new String[0]));
        } catch (IOException e) {
            logger.error("Problem reading tickers in config", e);
        }
    }

    private void readZacksSectors() {
        tickers = new String[0];
        try {
            tickers = FileUtils.readLines(new File("conf/Zacks-sectors.txt")).toArray((new String[0]));
        } catch (IOException e) {
            logger.error("Problem reading tickers in config", e);
        }
    }
    
    /**
     * @return the start
     */
    public Date getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Date end) {
        this.end = end;
    }

    /**
     * @return the totalLimit
     */
    public int getTotalLimit() {
        return totalLimit;
    }

    /**
     * @param totalLimit the totalLimit to set
     */
    public void setTotalLimit(int totalLimit) {
        this.totalLimit = totalLimit;
    }

    /**
     * @return the perCompanyLimit
     */
    public int getPerCompanyLimit() {
        return perCompanyLimit;
    }

    /**
     * @param perCompanyLimit the perCompanyLimit to set
     */
    public void setPerCompanyLimit(int perCompanyLimit) {
        this.perCompanyLimit = perCompanyLimit;
    }

    /**
     * @return the tickers
     */
    public String[] getTickers() {
        return tickers;
    }

    /**
     * @param tickers the tickers to set
     */
    public void setTickers(String[] tickers) {
        this.tickers = tickers;
    }

    /**
     * @return the downloadDir
     */
    public String getFirefoxDownloadDir() {
        return firefoxDownloadDir;
    }

    /**
     * @param firefoxDownloadDir the downloadDir to set
     */
    public void setFirefoxDownloadDir(String firefoxDownloadDir) {
        this.firefoxDownloadDir = firefoxDownloadDir;
    }

    /**
     * @return the myDownloadDir
     */
    public String getMyDownloadDir() {
        return myDownloadDir;
    }

    /**
     * @param myDownloadDir the myDownloadDir to set
     */
    public void setMyDownloadDir(String myDownloadDir) {
        this.myDownloadDir = myDownloadDir;
    }

    /**
     * @return the selectedTickers
     */
    public int[] getSelectedTickers() {
        return selectedTickers;
    }

    /**
     * @param selectedTickers the selectedTickers to set
     */
    public void setSelectedTickers(int[] selectedTickers) {
        this.selectedTickers = selectedTickers;
    }

    public static enum OS {

        LINUX, WINDOWS, MACOSX, UNKNOWN
    };

    /**
     * Determine the underlying OS.
     *
     * @return OS on which we are running
     */
    public static OS getOs() {
        String platform = System.getProperty("os.name").toLowerCase();
        if (platform.startsWith("windows")) {
            return OS.WINDOWS;
        } else if (platform.startsWith("linux")) {
            return OS.LINUX;
        } else if (platform.startsWith("mac os x")) {
            return OS.MACOSX;
        } else {
            return OS.UNKNOWN;
        }
    }

//    private final void readCapitalIqTickers() {
//        tickers = new String[0];
//        try {
//            tickers = FileUtils.readLines(new File("conf/CapitalIQ-tickers.txt")).toArray((new String[0]));
//        } catch (IOException e) {
//            logger.error("Problem reading the tags", e);
//        }
//    }
//    
//    private final void readJPMorganTickers() {
//        tickers = new String[0];
//        try {
//            tickers = FileUtils.readLines(new File("conf/JPM-tickers.txt")).toArray((new String[0]));
//        } catch (IOException e) {
//            logger.error("Problem reading the tickers in 'conf'", e);
//        }
//    }
    /**
     * @return the selected tags
     */
    public String[] getSelectedTickersByName() {
        List<String> sTags = new ArrayList<>();
        for (int i = 0; i < selectedTickers.length; ++i) {
            sTags.add(tickers[selectedTickers[i]]);
        }
        return sTags.toArray(new String[0]);
    }

    public boolean isDownloadPDF() {
        return format == FORMAT.PDF;
    }

    public boolean isDownloadHTML() {
        return format == FORMAT.HTML;
    }

    public void setDownloadPDF() {
        format = FORMAT.PDF;
    }

    public void setDownloadHTML() {
        format = FORMAT.HTML;
    }

    private void readCredentials() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("conf/credentials.properties"));
            capIqUsername = (String) props.get("capIqUsername");
            assert (capIqUsername != null);
            capIqPassword = (String) props.get("capIqPassword");
            assert (capIqPassword != null);
            // no assertions - if the keys are not given but upload is not requested, it's OK
            amazonId = (String) props.get("Access_Key_ID");
            amazonKey = (String) props.get("Secret_Access_Key");
            bucket = (String) props.get("upload_bucket");
        } catch (IOException e) {
            logger.error("Problem reading credentials", e);
        }
    }
    /**
     * @return the earliestYear
     */
    public int getEarliestYear() {
        return earliestYear;
    }

    /**
     * @param earliestYear the earliestYear to set
     */
    public void setEarliestYear(int earliestYear) {
        this.earliestYear = earliestYear;
    }

    /**
     * @return the emptyPagesToStop
     */
    public int getEmptyPagesToStop() {
        return emptyPagesToStop;
    }

    /**
     * @param emptyPagesToStop the emptyPagesToStop to set
     */
    public void setEmptyPagesToStop(int emptyPagesToStop) {
        this.emptyPagesToStop = emptyPagesToStop;
    }
    public String getTickerFile() {
        return tickerFile;
    }

    public void setTickerFile(String tickerFile) {
        this.tickerFile = tickerFile;
    }
}
