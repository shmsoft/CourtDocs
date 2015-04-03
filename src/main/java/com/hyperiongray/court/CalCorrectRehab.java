package com.hyperiongray.court;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CalCorrectRehab {
    private static String startPage = "http://www.cdcr.ca.gov/Reports_Research/Offender_Information_Services_Branch/Monthly/Monthly_Tpop1a_Archive.html";

    private static final Logger logger = LoggerFactory.getLogger(CalCorrectRehab.class);
    private static Options options;
    private String outputFileName;
    private boolean zipThrough;

    private long totalInputLines;
    private long totalImagesToProcess;
    private long totalImagesSuccessfullyProcessed;
    private long totalLinesWithImages;
    public static void main(String [] args) {
        formOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("MemexOCR - downloads and OCR's images in a CCA file", options);
            return;
        }
        // TODO parse and use options
        CalCorrectRehab instance = new CalCorrectRehab();
        try {
            if (!instance.parseParameters(args)) {
                return;
            }
            instance.downloadAndParse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String stats = "Processing stats:" + "\n" +
                "Lines processed: " +
                "Lines processed: " + instance.totalInputLines + "\n" +
                "Lines with images: " + instance.totalLinesWithImages + "\n" +
                "Number of images to process: " + instance.totalImagesToProcess + "\n" +
                "Number of images successfully processed: " + instance.totalImagesSuccessfullyProcessed;
        System.out.println(stats);

    }

    private static void formOptions() {
        options = new Options();
        options.addOption("o", "output", true, "Output file");
        options.addOption("z", "zipThrough", false, "Zip through the input web site but don't download or analyze PDF's");
    }
    private boolean parseParameters(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        outputFileName = cmd.getOptionValue("output");
        zipThrough = cmd.hasOption("zipthrough");
        if (!zipThrough) {
            if (outputFileName == null) {
                System.out.println("Please provide output file name");
                return false;
            }
        }
        return true;
    }
    private void downloadAndParse() throws IOException {

    }
}
