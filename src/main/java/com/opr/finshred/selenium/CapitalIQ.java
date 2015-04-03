package com.opr.finshred.selenium;

import com.google.common.annotations.VisibleForTesting;
import com.opr.finshred.Settings;
import com.opr.finshred.Util;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automate file downloads from CapitalIQ. IMPORTANT: browser should be set up
 * with automatic, no-confirm download on click. At the first download, the
 * operator should click on "Do this automatically for files of this type"
 *
 * @author mark
 */
public class CapitalIQ implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(com.opr.finshred.selenium.CapitalIQ.class);
    private int totalCount;
    private int perTickerCount;
    private boolean stop = false;
    private String downloadTimeStamp;
    private WebDriver driver;
    private WebElement query;

    public CapitalIQ() {
        logger.debug("Initiated CapitalIQ instance");
    }

    @Override
    public void run() {
        Date startDate = new Date();
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
        downloadTimeStamp = fm.format(startDate);
        Settings settings = Settings.getSettings();
        boolean firstDownload = true;
        Util.cleanupDownloadDir();
        driver = new FirefoxDriver();
        prepareForTranscriptSearch();
        // chose the company ticker
        String[] tickers = Settings.getSettings().getSelectedTickersByName();
        logger.info("Ready to search for {} tickers", tickers.length);
        totalCount = 0;
        for (String ticker : tickers) {
            try {
                perTickerCount = 0;
                logger.info("Searching for ticker {}", ticker);
                // clear up the previous ticker selection
                logger.debug("clear up the previous ticker selection");
                try {
                    query = driver.findElement(By.id("_criteria__searchSection__searchToggle__btGo__cancelLink"));
                    query.click();
                } catch (Exception e) {
                    logger.debug("No clear selection, but it is OK", e);
                }
                // select one ticker
                logger.debug("select ticker {}", ticker);
                query = driver.findElement(By.name(
                        "_criteria$_searchSection$_searchToggle$_criteria__searchSection__searchToggle__entitySearch_searchbox"));
                logger.debug("fill out the ticker name");
                query.sendKeys(ticker);
                // this does the auto-suggests, not caught by the browser request/response. 
                Util.sleep(5);
                //((JavascriptExecutor) driver).executeScript("void(0)");
                query.sendKeys(Keys.ARROW_DOWN);
                Util.sleep(1);
                query.sendKeys(Keys.RETURN);
                Util.sleep(1);
                // click on the company search button    
                logger.debug("Select date range button");
                query = driver.findElement(By.id("_criteria__searchSection__searchToggle__dateRange_DateRangeButton"));
                query.click();
                // prepare the date loop, going back a year at a time    
                int emptyPages = 0;
                int year = Calendar.getInstance().get(Calendar.YEAR);
                while (year >= settings.getEarliestYear()) {
                    String html = "";
                    try {
                        query = driver.findElement(By.id("_criteria__searchSection__searchToggle__dateRange_myFromBox"));
                        query.sendKeys("01/01/" + Integer.toString(year));
                        query = driver.findElement(By.id("_criteria__searchSection__searchToggle__dateRange_myToBox"));
                        query.sendKeys("01/01/" + Integer.toString(year + 1));
                        // click on the results search button
                        logger.debug("Search for documents, year {}", year);
                        query = driver.findElement(By.id("_criteria__searchSection__searchToggle__btGo__saveBtn"));
                        query.click();                    // get complete html and parse it
                        html = driver.getPageSource();
                    } catch (Exception e) {
                        logger.error("Problem with year {}", year, e);
                        --year;
                        continue;
                    }
                    List<String> ids = findPdfIds(html);
                    if (ids.isEmpty()) {
                        ++emptyPages;
                        logger.debug("Empty page number {}", emptyPages);
                    }
                    if (settings.getEmptyPagesToStop() > 0 && emptyPages >= settings.getEmptyPagesToStop()) {
                        break;
                    }
                    logger.debug("Preparing to download {} documents for the ticker {}", ids.size(), ticker);
                    for (String id : ids) {
                        logger.debug("Downloading for {}", id);
                        try {
                            if (isStop()) {
                                break;
                            }
                            // find the first PDF to download
                            query = driver.findElement(By.id(id));
                            // request it
                            query.click();
                            // confirm download - first time done by operator, later automatic     
                            if (firstDownload) {
                                firstDownload = false;
                                // Downloads usually take time. Give it initial wait.
                                Util.sleep(10);
                            }
                            ++perTickerCount;
                            if (settings.getPerCompanyLimit() > 0
                                    && perTickerCount >= settings.getPerCompanyLimit()) {
                                break;
                            }
                            // TODO make sure we don't break off the last download
                        } catch (Exception e) {
                            logger.warn("Problem download with id {}, going to the next one ", id, e);
                        }
                    }
                    if (isStop()) {
                        break;
                    }
                    if (settings.getPerCompanyLimit() > 0
                            && perTickerCount >= settings.getPerCompanyLimit()) {
                        break;
                    }
                    --year;
                }
                ++totalCount;
                if (settings.getTotalLimit() > 0 && totalCount >= settings.getTotalLimit()) {
                    break;
                }
            } catch (Exception e) {
                logger.warn("Problem with ticker {}, going to the next one", ticker, e);
                driver.get("https://www.capitaliq.com/CIQDotNet/Transcripts/Summary.aspx");
            } finally {
                new Util().moveDownloadedFiles(ticker, Settings.SITES.CapitalIQ, downloadTimeStamp);
            }
        }
        long duration = (new Date().getTime() - startDate.getTime()) / 1000;
        logger.info("Duration in seconds: {}", duration);
    }

    public static void main(String[] args) throws Exception {
        CapitalIQ instance = new CapitalIQ();
        Settings settings = Settings.getSettings();
        String[] tickers = {"AAPL", "GOOG", "MSFT"};
        settings.setTickers(tickers);
        settings.setSiteAndReadTickers(Settings.SITES.CapitalIQ.toString());
        settings.setTotalLimit(10);
        settings.setPerCompanyLimit(3);
        instance.run();
        System.exit(0);
    }

    /**
     * Find all PDF id's on the html page for CapIQ
     *
     * @param html page source
     * @return all id's
     */
    @VisibleForTesting
    List<String> findPdfIds(String html) {
        List<String> ids = new ArrayList<>();
        // find 1st, 3d, 5th, etc. marker for PDF
        boolean odd = false;
        String pdfMarker = "PDF_ReportImage";
        int start = 0;
        while (true) {
            start = html.indexOf(pdfMarker, start);
            if (start < 0) {
                break;
            }
            int idStart = html.lastIndexOf("'", start);
            int idEnd = html.indexOf("'", start);
            if (idStart < 0 || idEnd < 0) {
                break;
            }
            odd = !odd;
            if (odd) {
                ids.add(html.substring(idStart + 1, idEnd));
            }
            start = idEnd + 1;
        }
        // the last on the page is not a PDF link, remove it
        if (ids.size() > 0) {
            ids.remove(ids.size() - 1);
        }
        return ids;
    }

    /**
     * @return the stop
     */
    synchronized public boolean isStop() {
        return stop;
    }

    /**
     * @param stop the stop to set
     */
    synchronized public void setStop(boolean stop) {
        this.stop = stop;
    }

    private void prepareForTranscriptSearch() {

        Settings settings = Settings.getSettings();
        // Go to the CapitalIQ login page
        driver.get("https://www.capitaliq.com/ciqdotnet/login.aspx");
        // put in the username
        query = driver.findElement(By.name("myLogin$myUsername"));
        query.sendKeys(settings.getCapIqUsername());
        // put in the password
        query = driver.findElement(By.name("myLogin$myPassword"));
        query.sendKeys(settings.getCapIqPassword());
        // click on the login button
        query = driver.findElement(By.name("myLogin$myLoginButton"));
        query.click();
        // now go to the target page
        driver.get("https://www.capitaliq.com/CIQDotNet/Transcripts/Summary.aspx");

    }
}
