package com.hyperiongray.ocr;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hyperiongray.ocr.com.hyperiongray.data.CCAJasonParser;
import com.hyperiongray.ocr.com.hyperiongray.util.PlatformUtil;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.cli.ParseException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Options options;
    private String ccaInputFile;
    private String outputFileName;
    private boolean zipThrough;

    private long totalInputLines;
    private long totalImagesToProcess;
    private long totalImagesSuccessfullyProcessed;
    private long totalLinesWithImages;

    public static void main(String args[]) {
        formOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("MemexOCR - downloads and OCR's images in a CCA file", options);
            return;
        }
        // TODO parse and use options
        Main main = new Main();
        try {
            if (!main.parseParameters(args)) {
                return;
            }
            main.downloadAndOCR();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String stats = "Processing stats:" + "\n" +
                "Lines processed: " +
                "Lines processed: " + main.totalInputLines + "\n" +
                "Lines with images: " + main.totalLinesWithImages + "\n" +
                "Number of images to process: " + main.totalImagesToProcess + "\n" +
                "Number of images successfully processed: " + main.totalImagesSuccessfullyProcessed;
        System.out.println(stats);

    }

    private static void formOptions() {
        options = new Options();
        options.addOption("f", "file", true, "Input CCA file");
        options.addOption("o", "output", true, "Output file");
        options.addOption("z", "zipthrough", false, "Zip through the input but don't download or OCR");
    }


    private void downloadAndOCR() throws IOException {
        if (!zipThrough) {
            new File(new File(outputFileName).getParent()).mkdirs();
        }
        // read lines one at a time
        LineIterator it = FileUtils.lineIterator(new File(ccaInputFile), "UTF-8");
        try {
            while (it.hasNext()) {
                ++totalInputLines;
                String line = it.nextLine();
                String urls[] = new CCAJasonParser().getImageUrls(line);
                if (urls != null) {
                    ++totalLinesWithImages;
                    for (String url : urls) {
                        ++totalImagesToProcess;
                        if (!zipThrough) {
                            OCRConfiguration conf = new OCRConfiguration();
                            conf.setPdfImageExtractionDir("test-output/ocr/out/");
                            conf.setTesseractWorkDir("test-output/ocr/out/");

                            File f = new File("test-output/ocr/out");
                            f.mkdirs();
                            PlatformUtil.runCommand("wget -P test-output " + url);
                            OCRProcessor processor = OCRProcessor.createProcessor(conf);
                            String fileName = new URL(url).getFile().substring(1);
                            System.out.println("Image: " + fileName);
                            // slashes in the file name gives us a problem, they come from imageshack, skip them for now
                            if (!fileName.contains("/")) {
                                List<String> data = processor.getImageText("test-output/" + fileName);
                                if (data.size() > 0) {
                                    Gson gson = new GsonBuilder().create();
                                    OutputJson outputJson = new OutputJson();
                                    outputJson.image_url = url;
                                    outputJson.text = data.get(0);
                                    String json = gson.toJson(outputJson);
                                    ++totalImagesSuccessfullyProcessed;
                                    //System.out.println(json);
                                    Files.append(json + '\n', new File(outputFileName), Charset.defaultCharset());
                                }

                            }
                        }
                    }
                }

            }
        } finally {
            it.close();
        }
    }

    private boolean parseParameters(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        ccaInputFile = cmd.getOptionValue("file");
        if (ccaInputFile == null) {
            System.out.println("Please provide input file name");
        }
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

    private class OutputJson {
        private String image_url;
        private String text;
    }
}

