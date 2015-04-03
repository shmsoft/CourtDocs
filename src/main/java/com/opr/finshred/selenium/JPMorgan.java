package com.opr.finshred.selenium;

import com.google.common.annotations.VisibleForTesting;
import com.opr.finshred.Settings;
import com.opr.finshred.Util;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automate file downloads from JPMorgan. IMPORTANT: browser should be set up with automatic,
 * no-confirm download on click. At the first download, the operator should click on "Do this
 * automatically for files of this type"
 *
 * @author mark
 */
public class JPMorgan implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(com.opr.finshred.selenium.JPMorgan.class);
    private int totalCount;
    private int perTickerCount;
    private String downloadTimeStamp;
    private boolean stop = false;

    public JPMorgan() {
        logger.debug("Initiated JPMorgan instance");
    }

    @Override
    public void run() {
        Date startDate = new Date();
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
        downloadTimeStamp = fm.format(startDate);
        boolean firstDownload = true;
        Util.cleanupDownloadDir();
        WebDriver driver = new FirefoxDriver();
        // Go to the login page
        driver.get("https://jpmm.com");
        // put in the username
        WebElement query = driver.findElement(By.name("userName"));
        query.sendKeys("ocedar");
        // put in the password
        query = driver.findElement(By.name("pin_securIDToken"));
        query.sendKeys("omegapoint42");
        ((JavascriptExecutor) driver).executeScript("javascript:encryption();");

        // click on the login button
        // now go to the target page
        driver.get("https://markets.jpmorgan.com/#research.browse.companies");
        // chose the company ticker
        Settings settings = Settings.getSettings();
        String[] tickers = Settings.getSettings().getSelectedTickersByName();
        logger.info("Ready to search for {} tickers", tickers.length);
        totalCount = 0;
        String html;
        String companyPage = "https://markets.jpmorgan.com/research/CFP?page=company_page&companyId=";
        for (String ticker : tickers) {
            perTickerCount = 0;
            logger.debug("Select ticker {}", ticker);
            boolean isTickerTracked = false;
            driver.get(companyPage + ticker);
            html = driver.getPageSource();
            if (html.contains("You are not authorized to view selected company details")) {
                // Company not tracked. We will use generic regular search box.
                isTickerTracked = false;
                // pause as if in consternation at not found the company. We don't want the logs to
                // record our synthetic download attemps in close time
                Util.sleep(5);
            } else {
                isTickerTracked = true;
            }

            boolean morePages = true;
            int pageNumber = 0;
            while (morePages) {
                goToStartPage(driver, isTickerTracked, ticker);
                ++pageNumber;
                for (int skip = 1; skip < pageNumber; ++skip) {
                    // skip previous pages, if any, and go to the next ones, if any :)
                    try {
                        query = driver.findElement(By.className("increment"));
                        if (query.isEnabled()) {
                            query.click();
                        } else {
                            morePages = false;
                            break;
                        }
                        logger.debug("Skipping page {}", skip);
                    } catch (Exception e) {
                        // No? - Just continue with the next ticker                                            
                        logger.debug("No next page, so go with the next ticker");
                        morePages = false;
                        break;
                    }
                }
                if (!morePages) {
                    continue;
                }
                logger.debug("Downloading from page {}", pageNumber);
                html = driver.getPageSource();
                List<String> ids = findDocIds(html);
                ids = removeDuplicates(ids);
                for (String id : ids) {
                    if (isStop()) {
                        break;
                    }
                    // find the next doc to download
                    driver.get("https://markets.jpmorgan.com" + id);
                    try {
                        query = driver.findElement(By.id("download"));
                        query.click();
                        if (firstDownload) {
                            firstDownload = false;
                            Util.sleep(20);
                        }
                        ++perTickerCount;
                        if (settings.getPerCompanyLimit() > 0
                                && perTickerCount >= settings.getPerCompanyLimit()) {
                            break;
                        }
                    } catch (Exception e) {
                        // OK, no download button there, just skip it
                    }
                }
                if (isStop()) {
                    break;
                }
                ++totalCount;
                if (settings.getTotalLimit() > 0 && totalCount >= settings.getTotalLimit()) {
                    break;
                }
            }
            new Util().moveDownloadedFiles(ticker, Settings.SITES.JPMorgan, downloadTimeStamp);
        }
        long duration = (new Date().getTime() - startDate.getTime()) / 1000;
        logger.info("Duration in seconds: {}", duration);
    }

    public static void main(String[] args) throws Exception {
        Settings settings = Settings.getSettings();
        String[] tickers = {"M.N"};
        settings.setTickers(tickers);
        settings.setSiteAndReadTickers(Settings.SITES.JPMorgan.toString());

        JPMorgan instance = new JPMorgan();
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
    List<String> findDocIds(String html) {
        List<String> ids = new ArrayList<>();
        String marker = "/research/ArticleServlet?doc";
        int start = 0;
        while (true) {
            start = html.indexOf(marker, start);
            if (start < 0) {
                break;
            }
            int idStart = html.lastIndexOf("\"", start);
            int idEnd = html.indexOf("\"", start);
            if (idStart < 0 || idEnd < 0) {
                break;
            }
            String id = html.substring(idStart + 1, idEnd);
            // that's not all. We need to refer to a different servlet
            id = id.replace("ArticleServlet", "PubServlet");
            int amp = id.indexOf("&");
            if (amp >= 0) {
                ids.add(id.substring(0, amp) + "&page=1&forcePdf=1&action=open");
            }
            start = idEnd + 1;
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

    private void goToStartPage(WebDriver driver, boolean isTickerTracked, String ticker) {
        if (isTickerTracked) {
            driver.get("https://markets.jpmorgan.com/research/CFP?page=company_page&companyId=" + ticker);
        } else {
            driver.get("https://markets.jpmorgan.com/#research.browse.companies");
            // this page may cause a popup, remote it
            ((JavascriptExecutor) driver).executeScript("javascript:removePopupBox();");
            driver.get("https://markets.jpmorgan.com/CFP_Research/CFP?page=advanced_search_page&searchTerm=" + ticker);
        }
    }

    /**
     * Remove duplicate URL, which may be an artifact of parsing
     *
     * @param ids list of URL.
     */
    private List<String> removeDuplicates(List<String> ids) {
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }
}
