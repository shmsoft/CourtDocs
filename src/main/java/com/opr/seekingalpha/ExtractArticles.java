package com.opr.seekingalpha;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.opr.finshred.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mark
 */
public class ExtractArticles {

    private String outputDir;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/");
    private static final Logger logger = LoggerFactory.getLogger(ExtractArticles.class);
    private Date today = new Date();
    private TranscriptStats stats = new TranscriptStats();
    private final String downloadJson = "logs/earnings_transcripts.json";

    public static void main(String args[]) throws IOException {
        String tableOfContents = args[0];
        String articleContents = args[1];
        ExtractArticles instance = new ExtractArticles();
        instance.setOutputDir(args[2]);
        instance.extractArticles(tableOfContents, articleContents);
    }

    /**
     * @param tableOfContents
     * @param articleContents
     * @throws IOException
     */
    public void extractArticles(String tableOfContents, String articleContents) throws IOException {
        TOC toc = TOC.readToc(tableOfContents);
        extractArticles(toc, articleContents);
    }

    private void extractArticles(TOC toc, String articleContents)
            throws FileNotFoundException, IOException {
        ExtractSettings settings = ExtractSettings.getInstance();

        new File(getOutputDir()).mkdirs();
        int count = 0;
        int totalCount = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(articleContents))) {
            while (in.ready()) {
                String line = in.readLine();
                Article article = new Article().parse(line);

                // skip malformed records and the header line
                if (article.id == null || article.id.equals("id")) {
                    continue;
                }
                ++totalCount;
                // TODO verify that earnings calls and transcripts correlate (or don't); until then do transcripts
                // if (!settings.isEarningsCalls()) {
                    if (settings.isTranscripts()) {
                        if (!toc.isTranscript(article.id)) {
                            continue;
                        }
                    }
                //}
                if (!fitsFilter(article)) {
                    continue;
                }
                // if we don't have the ticker, don't output it either
                String ticker = toc.getTicker(article.id);
                int comma = ticker.indexOf(",");
                if (comma >= 0) ticker = ticker.substring(0, comma);
                if (ticker.trim().isEmpty()) {
                    continue;
                }
                // if the company name is requested, take only those articles that have ticker
                String c = settings.getCompanyName().toLowerCase();
                if (!c.trim().isEmpty()) {
                    if (!c.equalsIgnoreCase(ticker)) {
                        continue;
                    }
                }
                ++count;
                String articleDate = toc.getArticleDate(article.id);
                articleDate = articleDate.substring(0, 10).replace("-", "");
                String outputDirWithDate = outputDir + "/" + dateFormat.format(today);
                new File(outputDirWithDate).mkdirs();
                String outputFileName = outputDirWithDate
                        + ticker + "_" + articleDate + ".html";
                Files.write(article.formatHtmlType(), new File(outputFileName),
                        Charset.defaultCharset());
                String year = articleDate.substring(0, 4);
                stats.updateCount(ticker, year);
                if (settings.getDocLimit() > 0) {
                    if (count >= settings.getDocLimit()) {
                        break;
                    }
                }
            }
        }
        logger.info("Wrote {} articles out of {} total to {}", count, totalCount, outputDir);
        writeStats();
    }

    private boolean fitsFilter(Article article) {
        String articleForSearches = article.formatForSearches();
        ExtractSettings settings = ExtractSettings.getInstance();
        String[] filters = settings.getFiltersAsArray();
        // if filters are present, check them
        if (filters.length > 0) {
            for (String filter : filters) {
                // for those starting with !, take only ones that do not fit, it's an exclusion
                if (filter.startsWith("!")) {
                    String exclude = filter.substring(1, filter.length());
                    if (articleForSearches.indexOf(exclude) >= 0) {
                        return false;
                    }
                } else {
                    // for others, take any that fits
                    if (articleForSearches.indexOf(filter) >= 0) {
                        return true;
                    }
                }
            }
            // otherwise, it's not a fit
            return false;
        } else {
            // if filters are not present, take all articles
            return true;
        }
    }
    private void writeStats() throws IOException {
        JsonParseTranscript parser = new JsonParseTranscript();
        String jsonAsString = parser.format(stats);
        Files.write(jsonAsString, new File(downloadJson), Charset.defaultCharset());
    }

    /**
     * @return the outputDir
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * @param outputDir the outputDir to set
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    private class Article {

        private String id;
        private String subject;
        private String contents;

        public Article parse(String line) {
            String[] items = line.split("\t");
            if (items.length == 3) {
                id = items[0];
                subject = items[1];
                contents = line.substring(id.length() + subject.length() + 2);
            }
            return this;
        }

        public String formatHtmlType() {
            return "<html><head><meta content=\"text/html\"></head>" +
                    contents +
                    "</html>";
        }
        public String formatForSearches() {
            if (contents == null) {
                return "";
            }
            if (contents.trim().isEmpty()) {
                return "";
            }
            // TODO potentially, remove extra spaces
            // TODO in general, why not build a Lucene index in memory?
            return contents.trim().toLowerCase();
        }
    }
}
