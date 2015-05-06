package com.hyperiongray.court;

import com.google.common.io.Files;
import com.hyperiongray.pull.CalCorrectRehabGetPull;
import org.apache.commons.cli.*;
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
    private static String siteToAnalyze = "http://www.cdcr.ca.gov/Reports_Research/Offender_Information_Services_Branch/Monthly/Monthly_Tpop1a_Archive.html";
    private static String linkRoot = "http://www.cdcr.ca.gov/Reports_Research/Offender_Information_Services_Branch/Monthly";

    private static final Logger logger = LoggerFactory.getLogger(NYAppealCollect.class);
    private static Options options;
    private String outputFileName;
    private int limit;
    private boolean zipThrough;
    private Date startTime;
    private long documentsCollected;

    private final static String[] regex = {
            "TOTAL IN-CUSTODY\\s*\\d+,*\\d*",
            "IN-STATE\\s*\\d+,*\\d*",
            "INSTITUTIONS/CAMPS\\s*\\d+,*\\d*",
            "IN-STATE CONTRACT BEDS\\s*\\d+,*\\d*",
            "DMH STATE HOSPITALS\\s*\\d+,*\\d*",
            "OUT OF STATE(COCF)\\s*\\d+,*\\d*",
            "PAROLE\\s*\\d+,*\\d*",
            "NON-CDC JURISDICTION #4\\s*\\d+,*\\d*",
            "OTHER POPULATIONS #6\\s*\\d+,*\\d*"
    };

    public static void main(String[] args) {
        formOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CalCorrectRehab - downloads and summarize PDF reports", options);
            return;
        }
        // TODO parse and use options
        NYAppealCollect instance = new NYAppealCollect();
        try {
            if (!instance.parseParameters(args)) {
                return;
            }
            instance.startTime = new Date();
            instance.downloadAndParse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String stats = "Processing stats:" + "\n" +
                "Site to analyze: " + siteToAnalyze + "\n" +
                "Documents processed: " + instance.documentsCollected + "\n" +
                "Processing time (sec): " + instance.getProcessingTime();
        System.out.println(stats);

    }

    private static void formOptions() {
        options = new Options();
        options.addOption("o", "output", true, "Output file");
        options.addOption("l", "limit", true, "Limit how many PDF to process (for testing)");
        options.addOption("z", "zipThrough", false, "Zip through the input web site but don't download or analyze PDF's");
    }

    private boolean parseParameters(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        outputFileName = cmd.getOptionValue("output");
        if (cmd.hasOption("limit")) {
            limit = Integer.parseInt(cmd.getOptionValue("limit"));
        }
        zipThrough = cmd.hasOption("zipThrough");
        if (!zipThrough) {
            if (outputFileName == null) {
                System.out.println("Please provide output file name");
                return false;
            }
        }
        return true;
    }

    private void downloadAndParse() throws IOException, TikaException {
        // it is a small collection, get it all
        CalCorrectRehabGetPull pull = new CalCorrectRehabGetPull();
        pull.setStartPage(siteToAnalyze);
        List<String> docCollection = pull.getPdfCollection();
        Tika tika = new Tika();
        for (String pdfLink : docCollection) {
            ++documentsCollected;
            if (limit > 0 && documentsCollected > limit) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            new File(new File(outputFileName).getParent()).mkdirs();
            appendToOutput("File: " + pdfLink);
            String pdfText = tika.parseToString(new URL(linkRoot + "/" + pdfLink));
            // this is only for storage, but for actual results the next two lines are not needed
            new File("test-output/pdf-txt").mkdirs();
            Files.write(pdfText, new File("test-output/pdf-txt/" + new File(pdfLink).getName() + ".txt"), Charset.defaultCharset());
            // ---------------------
            writeStats(pdfText);
        }

    }

    private long getProcessingTime() {
        return (new Date().getTime() - startTime.getTime()) / 1000;
    }

    private void writeStats(String pdfText) throws IOException {

        for (String reg: regex) {
            Matcher m = Pattern.compile(reg).matcher(pdfText);
            while (m.find()) {
                appendToOutput(m.group());
            }
        }
    }

    private void appendToOutput(String text) throws IOException {
        Files.append(text + "\n", new File(outputFileName), Charset.defaultCharset());
    }
    private void prepareOutput() {
        if (outputFileName != null) {
            new File(new File(outputFileName).getParent()).mkdirs();
            new File(outputFileName).delete();
        }

    }
}
