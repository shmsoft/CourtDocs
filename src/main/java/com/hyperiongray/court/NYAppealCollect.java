package com.hyperiongray.court;

import com.google.common.io.Files;
import com.hyperiongray.pull.CalCorrectRehabGetPull;
import com.hyperiongray.pull.GetPull;
import com.hyperiongray.pull.NYAppealGetPull;
import com.hyperiongray.pull.Pull;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NYAppealCollect {

    private static final Logger logger = LoggerFactory.getLogger(NYAppealCollect.class);
    private static Options options;
    private String outputDir;
    private Date startTime;
    private long documentsCollected;
    private boolean verify;


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
        String stats = "Processing stats:" + "\n" +
                "Documents processed: " + instance.documentsCollected + "\n";
        System.out.println(stats);
    }

    private static void formOptions() {
        options = new Options();
        options.addOption("o", "outputDir", true, "Output directory");
        options.addOption("v", "verify", false, "Verify that we are hitting the right files");
    }

    private boolean parseParameters(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        outputDir = cmd.getOptionValue("output");
        verify = cmd.hasOption("verify");
        if (!verify) {
            if (outputDir == null) {
                System.out.println("Please provide output directory");
                return false;
            }
        }
        return true;
    }

    private void downloadDocuments() {
        List <String> urls = new NYAppealUtil().listURLs();
        for (String url: urls) {
            logger.debug("URL for downloads: {}", url);
            GetPull pull = new GetPull();
            pull.setUrl(url);
            String htmlPage = pull.getResponse();
            List <String> downloadLinks = new NYAppealUtil().listDownloadLinks(htmlPage);
            for (String downloadLink: downloadLinks) {
                logger.debug("Download link: {}", downloadLink);
            }
        }
    }
    private void prepareOutput() throws IOException {
        if (outputDir != null) {
            new File(new File(outputDir).getParent()).mkdirs();
            FileUtils.cleanDirectory(new File(outputDir));
        }
    }
}
