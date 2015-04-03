/*
 * Regular downloads of SeekingAlpha earnings calls
 */
package com.opr.seekingalpha;

import com.google.common.annotations.VisibleForTesting;
import com.opr.finshred.Util;
import com.opr.finshred.Settings;
import com.opr.finshred.parse.ParseUtil;
import com.opr.s3.S3Agent;
import org.apache.commons.cli.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mark
 */
public class SeekingAlphaDownload {
    private static final Logger logger = LoggerFactory.getLogger(SeekingAlphaDownload.class);
    private static Options options;
    private Settings settings = Settings.getSettings();
    private String startUrl = "http://seekingalpha.com/search/transcripts?term=Earnings%20Call&all=true";
    private String tickerStartUrl = "http://seekingalpha.com/symbol/"; // add the ticker and the /transcripts
    private String bucket = "opr-solr-data";
    private final String downloadLog = "logs/earnings_transcripts.log";
    private final String downloadJson = "logs/earnings_transcripts.json";
    private int downloadLogLine = 0;
    private int maxPages = 1;
    private Map<String, Integer> downloadTranscriptMap = new HashMap<>();
    private TranscriptStats transcriptStats = new TranscriptStats();
    private List<String> tickerList = null;

    // Format of input date in SeekingAlpha
    // date example: 2014-12-17T21:25:00Z
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Format for outputting downloaded results (directory)
    private SimpleDateFormat outputDateDirFormat = new SimpleDateFormat("yyyy/MM/dd");

    // Format for outputting downloaded results (file name)
    private SimpleDateFormat outputDateFileFormat = new SimpleDateFormat("yyyyMMdd");

    // Format for decoding transcript date
    // Example: Dec. 17, 2014 11:29 PM ET
    private SimpleDateFormat transcriptDateFormat = new SimpleDateFormat("MMM. dd, yyyy");

    private int page;
    private boolean downloadNewTranscriptsDone = false;
    private final static String WEBCAST = "Earnings Call Webcast";

    // for Selenium downloads
    WebDriver driver;

    public static void main(String[] args) throws IOException {
        formOptions();
        String programMessage = "SeekingAlphaDownload - downloading earnings call transcripts from SeekingAlpha";
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(programMessage, options);
            return;
        }
        try {
            CommandLineParser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);
            SeekingAlphaDownload instance = new SeekingAlphaDownload();
            // set command line parameters
            if (cmd.getOptionValue("maxPages") != null) {
                instance.setMaxPages(Integer.parseInt(cmd.getOptionValue("maxPages")));
            }
            instance.setTickers(cmd.getOptionValue("tickerList"));
            // do the work
            instance.readDownloadLogs();
            instance.doDownloads();
        } catch (IOException e) {
            logger.error("Processing error", e);
        } catch (org.apache.commons.cli.ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(programMessage, options);
            logger.error("Input parameter problem", e);
        }
    }

    public void downloadWithSelenium() throws IOException {
        login();
        setMaxPages(settings.getMaxPages());
        setTickers(settings.getTickerFile());
        readDownloadLogs();
        doDownloads();
        driver.close();
    }

    private void setTickers(String tickersFile) throws IOException {
        if (tickersFile != null) {
            List<String> rawTickerList = Files.readLines(new File(tickersFile), Charset.defaultCharset());
            tickerList = new ArrayList<>();
            for (String ticker : rawTickerList) {
                tickerList.add(ticker.trim());
            }
        }
    }


    /**
     * @return the startUrl
     */
    public String getStartUrl() {
        return startUrl;
    }

    /**
     * @param startUrl the startUrl to set
     */
    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    private void doDownloads() throws IOException {
        if (tickerList != null) {
            login();
            for (String ticker : tickerList) {
                String tickerUrl = tickerStartUrl + ticker + "/transcripts";
                logger.info("Downloading for ticker {} from {}", ticker, tickerUrl);
                SeekingAlphaGetPull getRequest = new SeekingAlphaGetPull();
                getRequest.setUrl(tickerUrl);
                String pageHtml = getRequest.getResponse();
                Files.write(pageHtml, new File("output/seekingalpha/page_" + ticker + ".html"), Charset.defaultCharset());
                downloadTickerTranscripts(ticker, pageHtml);
            }
            driver.close();
        } else {
            for (page = 1; ; ++page) {
                String pullUrl = startUrl + "&page=" + page;
                logger.info("Downloading page: {}", pullUrl);
                SeekingAlphaGetPull getRequest = new SeekingAlphaGetPull();
                getRequest.setUrl(pullUrl);
                String pageHtml = getRequest.getResponse();
//            Files.write(pageHtml, new File("output/seekingalpha/page" + page + ".html"), Charset.defaultCharset());
                downloadTranscripts(pageHtml);
                if (downloadNewTranscriptsDone || page >= getMaxPages()) {
                    break;
                }
            }
        }
        writeDownloadLogs();
    }

    private void downloadTranscripts(String transcriptPage) throws IOException {
        String delim1 = "<div class=\"transcript_link\">";
        String delim2 = "</span>";
        List<String> fragments = ParseUtil.getAllBetweenDelimiters(transcriptPage, delim1, delim2);
        logger.debug("Found {} transcript links on the page {}", fragments.size(), page);
        List<DownloadItem> transcripts = downloadAndParseTranscripts(fragments);
        logger.info("Downloaded {} transcripts", transcripts.size());
    }

    private void downloadTickerTranscripts(String ticker, String transcriptPage) throws IOException {
        String delim = "earnings-call-transcript\" sasource";
        int index = 0;
        List<String> links = new ArrayList<>();
        while (index < transcriptPage.length()) {
            index = transcriptPage.indexOf(delim, index);
            if (index >= 0) {
                int start = transcriptPage.lastIndexOf("<a href=\"", index);
                if (start >= 0) {
                    start = start + "<a href=\"".length();
                    int end = transcriptPage.indexOf("\"", start);
                    if (end >= 0) {
                        links.add(transcriptPage.substring(start, end));
                        index = end;
                    }
                }
            } else {
                break;
            }
        }
        logger.debug("Found {} transcript links", links.size());
        List<DownloadItem> transcripts = downloadAndParseTranscriptLinks(ticker, links);
        logger.info("Downloaded {} transcripts", transcripts.size());
    }

    private void readDownloadLogs() throws IOException {
        S3Agent agent = new S3Agent();
        agent.setMyBucket(bucket);
        // parse regular log - this is needed to stop downloading on reaching already downloaded transcripts
        String downloadLogContents = agent.getFileFromS3AsString(downloadLog);
        // write it locally also, we will be appending to it
        Files.write(downloadLogContents.getBytes(), new File(downloadLog));
        String[] logEntries = downloadLogContents.split("\n");
        for (String entry : logEntries) {
            entry = entry.trim();
            // look for lines that end with html, since these are about transcripts
            if (!entry.isEmpty() && entry.endsWith(".html")) {
                int transcriptName = entry.lastIndexOf(" ");
                if (transcriptName >= 0) {
                    downloadTranscriptMap.put(entry.substring(transcriptName + 1), 1);
                }
            }
        }
        // parse JSON stats
        try {
            String downloadJsonContents = agent.getFileFromS3AsString(downloadJson);
            Files.write(downloadJsonContents, new File(downloadJson), Charset.defaultCharset());
            JsonParseTranscript parser = new JsonParseTranscript();
            transcriptStats = parser.parseEarningsTranscript(downloadJson);
        } catch (Exception e) {
            logger.warn("Problem downloading stats", e);
            // but the show still goes on
        }
    }

    /**
     * Write back the log we have been writing
     *
     * @throws IOException
     */
    private void writeDownloadLogs() throws IOException {
        // Log of downloaded entries
        S3Agent agent = new S3Agent();
        agent.setMyBucket(bucket);
        agent.setContentType("text/text");
        agent.putFileInS3(downloadLog, downloadLog);
        // JSON stats
        JsonParseTranscript parser = new JsonParseTranscript();
        String jsonAsString = parser.format(transcriptStats);
        Files.write(jsonAsString, new File(downloadJson), Charset.defaultCharset());
        agent.setContentType("application/json");
        agent.putFileInS3(downloadJson, downloadJson);
    }

    private List<DownloadItem> downloadAndParseTranscriptLinks(String ticker, List<String> links) throws IOException {
        List<DownloadItem> items = new ArrayList<>();
        for (String link : links) {
            DownloadItem item = new DownloadItem();
            item.setTicker(ticker);
            item.setUrl(link);
            item.setTranscriptHtml(downloadTranscriptSelenium(item.getUrl()));
            // right after download, wait for a second
            Util.sleep(1);
            item.setDate(getDate(item.getTranscriptHtml()));
            String fileName = formatFileName(item);
            // check if we already saw this transcript
            String entryName = new File(fileName).getName();
            // check that it is not a webcast (anchor at the beginning)
            int indexOfWebCast = item.getTranscriptHtml().indexOf(WEBCAST);
            if (indexOfWebCast >= 0 && indexOfWebCast < 300) {
                logger.debug("{} is a webcast", entryName);
                continue;
            }
            if (downloadTranscriptMap.containsKey(entryName)) {
                logger.debug("Been there, done that for {}", entryName);
                continue;
            }
            new File("output/" + fileName).getParentFile().mkdirs();
            Files.write(item.getTranscriptHtml(), new File("output/" + fileName), Charset.defaultCharset());
            // upload the transcript file to S3
            S3Agent agent = new S3Agent();
            agent.setMyBucket(bucket);
            agent.setContentType("text/html");
            agent.putFileInS3(fileName, "output/" + fileName);
            items.add(item);
            downloadTranscriptMap.put(entryName, 1);
            // TODO we may want to create the log line with SLF library
            ++downloadLogLine;
            String mes = new Date().toString() + " " + downloadLogLine +
                    " [main] INFO  com.opr.seekingalpha.SeekingAlphaDownload  - Downloaded earnings transcript " +
                    "with key " + fileName + " for "
                    + entryName + "\n";
            Files.append(mes, new File(downloadLog), Charset.defaultCharset());
            transcriptStats.updateCount(ticker, item.getYear());
        }
        return items;
    }

    private List<DownloadItem> downloadAndParseTranscripts(List<String> fragments) throws IOException {
        List<DownloadItem> items = new ArrayList<>();
        boolean newOnPage = false;
        for (String fragment : fragments) {
            DownloadItem item = new DownloadItem();
            item.setTicker(getTicker(fragment));
            // TODO getBetweenDelimiters seems to get an extra character up front - fix it and check other uses
            // Note the hack: the full link is misquoted by SeekingAlpha with %2B instead of %20, but we are stopping before that
            item.setUrl(ParseUtil.getBetweenDelimiters(fragment, "<a href=", "?all=true", 0).substring(1));
            item.setTranscriptHtml(downloadTranscript(item.getUrl()));
            // right after download, wait for a second
            Util.sleep(1);
            item.setDate(getDate(item.getTranscriptHtml()));
            String fileName = formatFileName(item);
            // check if we already saw this transcript
            String entryName = new File(fileName).getName();
            // check that it is not a webcast (anchor at the beginning)
            int indexOfWebCast = item.getTranscriptHtml().indexOf(WEBCAST);
            if (indexOfWebCast >= 0 && indexOfWebCast < 300) {
                logger.debug("{} is a webcast", entryName);
                continue;
            }
            if (downloadTranscriptMap.containsKey(entryName)) {
                logger.debug("Been there, done that for {}", entryName);
                continue;
            }
            newOnPage = true;
            new File("output/" + fileName).getParentFile().mkdirs();
            Files.write(item.getTranscriptHtml(), new File("output/" + fileName), Charset.defaultCharset());
            // upload the transcript file to S3
            S3Agent agent = new S3Agent();
            agent.setMyBucket(bucket);
            agent.setContentType("text/html");
            agent.putFileInS3(fileName, "output/" + fileName);
            items.add(item);
            downloadTranscriptMap.put(entryName, 1);
            // TODO we may want to create the log line with SLF library
            ++downloadLogLine;
            String mes = new Date().toString() + " " + downloadLogLine +
                    " [main] INFO  com.opr.seekingalpha.SeekingAlphaDownload  - Downloaded earnings transcript " +
                    "with key " + fileName + " for "
                    + entryName + "\n";
            Files.append(mes, new File(downloadLog), Charset.defaultCharset());
            transcriptStats.updateCount(item.getTicker(), item.getYear());
        }
        // If all transcripts on that page are already seen - we don't go any further
        if (!newOnPage) {
            downloadNewTranscriptsDone = true;
        }
        return items;
    }

    /**
     * key format:
     * earnings_transcripts/{YEAR_OF_UPLOAD}/{MONTH_OF_UPLOAD}/{DAY_OF_UPLOAD}/{TICKER}_{DATE_OF_TRANSCRIPT}.html
     * example: earnings_transcripts/2014/12/15/GRPN_20130814.html
     *
     * @param item
     * @return
     */
    private String formatFileName(DownloadItem item) {
        Date today = new Date();
        return "earnings_transcripts/" +
                outputDateDirFormat.format(today) + "/" +
                item.getTicker() + "_" + outputDateFileFormat.format(item.getDate()) +
                ".html";
    }

    @VisibleForTesting
    String getTicker(String fragment) {
        Pattern pattern = Pattern.compile("\\([A-Z]+\\)");
        Matcher matcher = pattern.matcher(fragment);
        if (matcher.find()) {
            //for (int m = 0; m < matcher.groupCount(); ++m) System.out.println(matcher.group(m));
            return matcher.group(0).substring(1, matcher.group(0).length() - 1);
        } else {
            return "";
        }
    }

    @VisibleForTesting
    Date getDate(String transcriptHtml) {
        int articleInfo = transcriptHtml.indexOf("article_info_pos");
        if (articleInfo >= 0) {
            String dateStr = ParseUtil.getBetweenDelimiters(transcriptHtml,
                    ">", "<", articleInfo + "article_info_pos".length() + 2);  // go beyond the first >
            if (dateStr.length() < 13) {
                return null;
            } else {
                // Note that the date format is strange, and we are dropping the last part of it, including hours/minutes
                dateStr = dateStr.substring(25, 38);
            }
            try {
                Date date = transcriptDateFormat.parse(dateStr);
                return date;
            } catch (ParseException e) {
                logger.warn("Could not parse the date {}", dateStr, e);
            }
        }
        return null;
    }

    private String downloadTranscriptSelenium(String url) throws IOException {
        driver.get("http://seekingalpha.com" + url);
        String pageHtml = driver.getPageSource();
        Files.write(pageHtml, new File("output/ticker.html"), Charset.defaultCharset());
        return pageHtml;
    }

    private String downloadTranscript(String url) {
        SeekingAlphaGetPull getRequest = new SeekingAlphaGetPull();
        getRequest.setUrl("http://seekingalpha.com" + url);
        String pageHtml = getRequest.getResponse();
        return pageHtml;
    }

    private static void formOptions() {
        options = new Options();
        options.addOption("m", "maxPages", true, "maximum number of pages from SeekingAlpha to crawl");
        // Note that for specific tickers we do a different search start page, and a different algorithm
        options.addOption("t", "tickerList", true, "Path to the ticker list of interest");
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public TranscriptStats getTranscriptStats() {
        return transcriptStats;
    }
    private void login() {
        logger.info("Logging in to http://seekingalpha.com");
        driver = new HtmlUnitDriver();
        driver = new FirefoxDriver();
        driver.get("http://seekingalpha.com");
        WebElement query = driver.findElement(By.id("user_settings_wrapper"));
        query.click();
        // wait 5 seconds for the login window to appear
        Util.sleep(5);
        Set windowHandles = driver.getWindowHandles();
        // fill out username and password, then click
        logger.info("Looking at " + driver.getCurrentUrl());
        String html = driver.getPageSource();
        logger.info(html);
    }
}
