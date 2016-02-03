package com.hyperiongray.court;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.google.common.io.Files;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyperiongray.court.parser.DataKey;
import com.hyperiongray.court.parser.IParser;
import com.hyperiongray.court.parser.TextParser;
import com.hyperiongray.court.parser.gate.GATEParser;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static Options options;
    private String inputDir;
    private String outputFile;
    private String parserType;
    private boolean rawMode;
    private String gateHome;
    private int breakSize = 10000;
    private int sample;

    public static void main(String[] args) {
        Application application = new Application();
        application.run(args);
    }

    private void run(String[] args) {
        formOptions();
        HelpFormatter formatter = new HelpFormatter();
        if (args.length == 0) {
            formatter.printHelp("NYAppealParse - extract legal information from court cases", options);
            return;
        }
        try {
            if (!parseOptions(args)) {
                return;
            }
            if (sample > 0) {
                doSampling();
                return;
            }
            if (parserType.equals(GATEParser.PARSER_TYPE) || parserType.equalsIgnoreCase(IParser.BOTH)) {
                if (StringUtils.isEmpty(gateHome)) {
                    formatter.printHelp("When GATE parser is used, gate-home parameter must be specified", options);
                    return;
                }
            }
            IParser parser = null;

            cleanupFirst();
            if (parserType.equalsIgnoreCase(TextParser.PARSER_TYPE)) {
                parser = new TextParser();
            } else if (parserType.equalsIgnoreCase(GATEParser.PARSER_TYPE)) {
                parser = new GATEParser(gateHome);
            } else if (parserType.equalsIgnoreCase(IParser.BOTH)) {
                IParser textParser = new TextParser();
                System.out.println("Using parser " + textParser.getClass().getSimpleName());
                parseAll(textParser);
                System.out.println(textParser.getStats().toString());

                IParser gateParser = new GATEParser(gateHome);
                System.out.println("Using parser " + gateParser.getClass().getSimpleName());
                parseAll(gateParser);
                System.out.println(gateParser.getStats().toString());

            } else {
                logger.info("Unrecognized parser type " + parserType + ", using NYAppealParse.");
                parser = new TextParser();
            }
            if (!parserType.equalsIgnoreCase(IParser.BOTH)) {
                System.out.println("Using parser " + parser.getClass().getSimpleName());
                parseAll(parser);
                System.out.println(parser.getStats().toString());
            }
            System.out.println("Input dir: " + inputDir);
            System.out.println("Output dir: " + new File(outputFile).getParent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void formOptions() {
        options = new Options();
        options.addOption("i", "inputDir", true, "Input directory");
        options.addOption("o", "outputFile", true, "Output file, .csv will be added");
        options.addOption("b", "breakSize", true, "Output file size in lines");
        options.addOption("p", "parser", true, "Parser type: 'text' by default, 'gate' or 'both'");
        options.addOption("r", "raw", false, "Raw mode, debugging mode, will produce *_raw.txt");
        options.addOption("g", "gate-home", true, "Path to GATE app directory (if using GATE parser)");
        // this is used to find representative cases
        options.addOption("s", "sample", true, "Find top s criminal cases, by doc length");
    }

    private boolean parseOptions(String[] args) throws org.apache.commons.cli.ParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        inputDir = cmd.getOptionValue("inputDir");
        outputFile = cmd.getOptionValue("outputFile");
        parserType = cmd.getOptionValue("parser");
        if (parserType == null) parserType = "text"; 
        gateHome = cmd.getOptionValue("gate-home");
        rawMode = cmd.hasOption("raw");
        if (cmd.hasOption("breakSize")) {
            breakSize = Integer.parseInt(cmd.getOptionValue("breakSize"));
        }
        sample = Integer.parseInt(cmd.getOptionValue("sample"));
        return true;
    }

    public void parseAll(IParser parser) throws IOException {
        int lineCount = 0;
        File[] files = new File(inputDir).listFiles();
        Arrays.sort(files);
        parser.getStats().set(DataKey.FiledInDir, files.length);
        FileUtils.write(new File(outputFile + parser.getStats().get(DataKey.FileNumber) + ".csv"), createHeader(), false);

        for (File file : files) {
            if (files == null) {
                logger.warn("No files found in input");
                return;
            }
            try {
                // right now, we analyze only "txt", and consider the rest as garbage
                if (!file.getName().endsWith("txt")) {
                    continue;
                }
                parser.getStats().inc(DataKey.Docs);
                StringBuffer buf = new StringBuffer();
                StringBuffer raw_buffer = new StringBuffer();
                Map<DataKey, String> answer = parser.parseFile(file);
                for (int e = 0; e < DataKey.values().length; ++e) {
                    DataKey key = DataKey.values()[e];
                    if (!key.isOutputToFile()) {
                        continue;
                    }
                    String value = "";
                    if (answer.containsKey(key)) {
                        value = answer.get(key);
                    }
                    buf.append(value).append(CommonConstants.CSV_FIELD_SEPARATOR);
                    raw_buffer.append(key.toString() + "=" + value + "\n");
                }
                raw_buffer.append("========\n");
                buf.deleteCharAt(buf.length() - 1);
                buf.append("\n");
                FileUtils.write(new File(outputFile + parser.getStats().get(DataKey.FileNumber) + ".csv"), buf.toString(), true);
                if (rawMode) {
                    FileUtils.write(new File(outputFile + parser.getStats().get(DataKey.FileNumber) + "_" +
                            parser.getClass().getSimpleName().toLowerCase() + "_raw.txt"), raw_buffer.toString(), true);
                }
                parser.getStats().inc(DataKey.Metadata);
                ++lineCount;
                if (lineCount >= breakSize) {
                    buf = new StringBuffer();
                    parser.getStats().inc(DataKey.FileNumber);
                    lineCount = 1;
                    // create new file, append = false
                    FileUtils.write(new File(outputFile + parser.getStats().get(DataKey.FileNumber) + ".csv"), createHeader(), false);
                    System.out.println("Writing parsed file " + parser.getStats().get(DataKey.FileNumber));
                }
            } catch (Exception e) {
                logger.error("Error processing file {} " + file.getName(), e);
            }
        }
    }

    public void doSampling() throws IOException {
        int sampleCount = 0;
        File[] files = new File(inputDir).listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File file1, File file2) {
                return Long.compare(file2.length(), file1.length());
            }
        });
        // TODO - refactor for better coding
        for (File file: files) {
            System.out.println(file.getPath());
            if (sampleCount >= sample) {
                break;
            }
            List<String> lines = Files.readLines(file, Charset.defaultCharset());
            for (int l = 0; l < lines.size(); ++l) {
                if (lines.get(l).contains("People v") && l < 5) {
                    String outputFileName = "/home/mark/greg" + "/" + file.getName();
                    Files.copy(file, new File(outputFileName));
                    ++sampleCount;
                    break;
                }
            }
        }
    }

    private void cleanupFirst() {
        try {
            File[] files = new File(outputFile).getParentFile().listFiles();
            for (File file : files) {
                if (file.getName().endsWith("csv")) file.delete();
                if (file.getName().endsWith("_raw.txt")) file.delete();
            }
        } catch (Exception e) {
            logger.warn("Cleaning exception, but that's OK", e);
        }
    }

    private String createHeader() throws IOException {
        StringBuffer buf = new StringBuffer();
        for (int e = 0; e < DataKey.values().length; ++e) {
            DataKey key = DataKey.values()[e];
            if (!key.isOutputToFile()) {
                continue;
            }
            buf.append(key).append(CommonConstants.CSV_FIELD_SEPARATOR);
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append("\n");
        return buf.toString();
    }


}
