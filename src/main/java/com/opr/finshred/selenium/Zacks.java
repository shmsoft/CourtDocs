package com.opr.finshred.selenium;

import com.google.common.annotations.VisibleForTesting;
import com.opr.finshred.Settings;
import com.opr.finshred.Util;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxProfile;
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
public class Zacks implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(com.opr.finshred.selenium.Zacks.class);
    
    private int perTickerCount;
    private String downloadTimeStamp;
    private boolean stop = false;

    public Zacks() {
        logger.debug("Initiated Zacks instance");
    }

    @Override
    public void run() {
        Date startDate = new Date();
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
        downloadTimeStamp = fm.format(startDate);
        Util.cleanupDownloadDir();        
        //create profile to download pdfs
        Settings settings = Settings.getSettings();
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf,application/x-pdf");        
        profile.setPreference("pdfjs.disabled",true);
        profile.setPreference("browser.download.dir", settings.getFirefoxDownloadDir()); 
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("plugin.disable_full_page_plugin_for_types", "application/pdf,application/vnd.adobe.xfdf,application/vnd.fdf,application/vnd.adobe.xdp+xml");
        
        
        WebDriver driver = new FirefoxDriver(profile);
        // Go to the login page
        driver.get("http://www.zacks.com/registration/premium_login.php");       
        
        // TODO get credentials from the file

        WebElement query = driver.findElement(By.id("user_name"));
        query.sendKeys("marcswarren@gmail.com");
        // put in the password
        query = driver.findElement(By.name("password"));
        query.sendKeys("qlab1234");

        // click on the login button
        query.submit();
        // now go to the target page
        driver.get("http://www.zacks.com/stock/research/t/equity-research?t=t&industry=");
        // chose the company ticker        
        String[] tickers = Settings.getSettings().getSelectedTickersByName();
        logger.info("Ready to search for {} tickers", tickers.length);
        int totalCount = 0;
        String html;
        String companyPage = "http://www.zacks.com/stock/research/";
        for (String ticker : tickers) {
            perTickerCount = 0;
            logger.debug("Select ticker {}", ticker);
            // TODO - if we hit too many tickers that are not tracked, they may block us
            // normally people don't hit on tickers that are not tracked, but robots do
            boolean isTickerTracked = false;
            driver.get(companyPage + ticker + "/equity-research?" + ticker + "&industry=");
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

            if (isTickerTracked) {
                logger.debug("Downloading from page {}", 1);
                try {
                    query = driver.findElement(By.xpath("//a[contains(@class,'report_')]"));
                    query.click();                    
                    Util.sleep(5);
                    
                    ++perTickerCount;
                    if (settings.getPerCompanyLimit() > 0
                            && perTickerCount >= settings.getPerCompanyLimit()) {
                        break;
                    }
                } catch (Exception e) {
                    logger.error("Problem downloading a page", e);
                }
            }

            new Util().moveDownloadedFiles(ticker, Settings.SITES.Zacks, downloadTimeStamp);
        }
        long duration = (new Date().getTime() - startDate.getTime()) / 1000;
        logger.info("Duration in seconds: {}", duration);
    }

    public static void main(String[] args) throws Exception {
        Settings settings = Settings.getSettings();
        String[] tickers = {"M.N"};
        settings.setTickers(tickers);
        settings.setSiteAndReadTickers(Settings.SITES.Zacks.toString());

        Zacks instance = new Zacks();
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
        String id = html.split("/")[html.split("/").length - 1];
        ids.add(id);
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
}
