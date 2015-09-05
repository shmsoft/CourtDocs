package com.hyperiongray.court;

import com.google.common.io.Files;
import com.hyperiongray.court.parser.NYAppealUtil;
import com.hyperiongray.pull.GetPull;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public class NYAppealCollect {

    private static final Logger logger = LoggerFactory.getLogger(NYAppealCollect.class);
    private static Options options;
    private String outputDir;
    private Date startTime;
    private int documentsCollected;
    private boolean verify;
    private float sample = 1; // default is to download all
    private int delay = 20; // milliseconds

    public static void main(String[] args) {
        formOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("NYAppealCollect - downloads and summarize PDF reports", options);
            return;
        }
        NYAppealCollect instance = new NYAppealCollect();
        try {
            if (!instance.parseParameters(args)) {
                return;
            }
            instance.startTime = new Date();
            instance.prepareOutput();
            instance.downloadDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Processing stats:");
        logger.info("Documents processed: {}", instance.documentsCollected);
    }

    private static void formOptions() {
        options = new Options();
        options.addOption("o", "outputDir", true, "Output directory");
        options.addOption("v", "verify", false, "Verify that we are hitting the right urls, do no downloads");
        options.addOption("s", "sample", true, "Sample the data, i.e. .01 means download only 1% of all files");
    }

    private boolean parseParameters(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        outputDir = cmd.getOptionValue("outputDir");
        verify = cmd.hasOption("verify");
        if (!verify) {
            if (outputDir == null) {
                System.out.println("Please provide output directory");
                return false;
            }
        }
        String sampleStr = cmd.getOptionValue("sample");
        if (sampleStr != null) {
            try {
                sample = Float.parseFloat(sampleStr);
            } catch (NumberFormatException e) {
                logger.error("Wrong sample frequency format");
                return false;
            }
        }
        return true;
    }

    private void downloadDocuments() {
        List<String> urls = new NYAppealUtil().listURLs();
        Tika tika = new Tika();
        for (String url : urls) {
            logger.debug("URL for downloads: {}", url);
            GetPull pull = new GetPull();
            pull.setUrl(url);
            String htmlPage = pull.getResponse();
            List<String> downloadLinks = new NYAppealUtil().listDownloadLinks(htmlPage);
            Metadata metadata = new Metadata();
            for (String downloadLink : downloadLinks) {
                logger.debug("Download link: {}", downloadLink);
                ++documentsCollected;
                if (verify) {
                    // verify means don't download, just list
                    continue;
                }
                if (Math.random() > sample) {
                    continue;
                }
                try {
                    // original html
                    pull.setUrl(downloadLink);
                    String courtDoc = pull.getResponse();
                    String fileName = new File(downloadLink).getName();
                    Files.write(courtDoc, new File(outputDir + "/html/" + fileName), Charset.defaultCharset());
                    NYAppealUtil.sleep(delay);
                    
                    // parsed as text
                    InputStream stream = new ByteArrayInputStream(courtDoc.getBytes(StandardCharsets.UTF_8));
                    String htmlText = tika.parseToString(stream, metadata);
                    
                    // TODO - we are not using metadata as yet, but it may be a good idea
                    Files.write(htmlText, new File(outputDir + "/txt/" + fileName + ".txt"), Charset.defaultCharset());

                } catch (IOException | TikaException e) {
                    logger.error("Problem downloading {}", downloadLink);
                }
            }
            NYAppealUtil.sleep(delay);
        }
    }

    private void prepareOutput() throws IOException {
        if (outputDir != null) {
            new File(outputDir).mkdirs();
            FileUtils.cleanDirectory(new File(outputDir));
            new File(outputDir + "/html").mkdirs();
            new File(outputDir + "/txt").mkdirs();

        }
    }
}
