package com.hyperiongray.court;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mark on 5/20/15.
 */
public class NYAppealParse {
    private static final Logger logger = LoggerFactory.getLogger(NYAppealParse.class);
    private static Options options;

    public enum KEYS {
        File, Casenumber, CivilKriminal, Court, County, Judge, DistrictAttorney, Keywords, FirstDate, AppealDate,
        Gap_days, ModeOfConviction, Crimes, Judges, Defense, DefendantAppellant, DefendantRespondent,
        HarmlessError, NotHarmlessError, DocumentLength, SexOffender
    }

    private final static int MAX_FIELD_LENGTH = 100; // more than that is probably a bug, so don't make it a parameter
    private Stats stats = new Stats();

    private String inputDir;
    private String outputFile;
    private int breakSize = 10000;
    private int fileNumber = 0;
    private char separator = '|';
    private String months = "(January|February|March|April|May|June|July|August|September|October|November|December)";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMdd,yyyy");
    String [] keywords = {"unanimouslys*affirmed|affirmed",
            "unanimouslys*modified|modified",
            "unanimouslys*reversed|reversed",
            "unanimouslys*dismissed|dismissed",
            "cases*iss*held",
            "decisions*iss*reserved",
            "matters*iss*remitted"}
            ;

    boolean sexOffender = false;
    String sexOffenderKeywords = "sex\\s*offender\\s*registration\\s*act";

    public Map<String, String> extractInfo(File file) throws IOException {
        String text = FileUtils.readFileToString(file);
        String textFlow = text.replaceAll("\\r\\n|\\r|\\n", " ");

        //System.out.println("Text flow: " + textFlow);
        Map<String, String> info = new HashMap<>();
        // there are so many exceptions that 'case' is preferable to a generic loops with exceptions
        Matcher m;
        String regex = "";
        String value = "";
        // civil vs criminal
        boolean criminal;
        regex = "People v ";
        m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        criminal = m.find();
        // sex offender
        regex = "sex\\s*offender\\s*registration\\s*act";
        m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        sexOffender = m.find();
        if (sexOffender) criminal = true;

        for (int e = 0; e < KEYS.values().length; ++e) {
            KEYS key = KEYS.values()[e];
            value = "";

            info.put(key.toString(), value);

            switch (key) {
                case File:
                    info.put(KEYS.File.toString(), file.getName());
                    continue;
                case Casenumber:
                    value = "";
                    regex = "\\[[0-9]+\\sAD.+\\]";
                    m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                    if (m.find()) {
                        value = sanitize(m.group());
                        if (value.length() >= 3 && value.length() < 15 && value.contains("AD")) {
                            info.put(key.toString(), value);
                        }
                    }
                    if (value.isEmpty()) {
                        regex = "[0-9]{4}.+NY.*Slip.*Op.*[0-9]+";
                        m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                        if (m.find()) {
                            value = sanitize(m.group());
                            info.put(key.toString(), value);
                        }
                    }
                    if (!value.isEmpty()) ++stats.caseNumber;
                    continue;

                case CivilKriminal:
                    info.put(KEYS.CivilKriminal.toString(), criminal ? "K" : "C");
                    if (criminal) {
                        ++stats.criminal;
                    } else {
                        ++stats.civil;
                    }
                    continue;

                case SexOffender:
                    info.put(KEYS.SexOffender.toString(), sexOffender? "Y" : "");
                    if (sexOffender) {
                        ++stats.sexOffence;
                    }
                    continue;

                case DocumentLength:
                    info.put(KEYS.DocumentLength.toString(), Integer.toString(text.length()));
                    continue;
                case Court:
                    value = "";
                    if (text.contains("Supreme Court")) value = "Supreme Court";
                    if (text.contains("County Court")) value = "County Court";
                    if (text.contains("Court of Claims")) value = "Court of Claims";
                    info.put(key.toString(), value);
                    if (value.isEmpty()) ++stats.courtProblem;
                    continue;
                case County:
                    regex = "[a-zA-Z]+\\sCounty";
                    m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                    if (m.find()) {
                        info.put(key.toString(), sanitize(m.group()));
                    }
                    continue;

                case Judge:
                    regex = "(Court|County|Court of Claims) \\(.+?\\)";
                    m = Pattern.compile(regex).matcher(textFlow);
                    value = "";
                    if (m.find()) {
                        value = m.group();
                        value = betweenTheLions(value, '(', ')');
                        info.put(key.toString(), sanitize(value));
                        ++stats.judge;
                    }
                    continue;

                case Keywords:
                        info.put(key.toString(), findAll(text, keywords));
                    continue;

                case FirstDate:
                    regex = "(rendered|entered|dated) " + months + " [0-9]+?, 2[0-1][0-9][0-9]";
                    m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                    if (m.find()) {
                        value = m.group();
                        value = value.replaceAll("rendered ", "");
                        value = value.replaceAll("entered ", "");
                        value = value.replaceAll("dated ", "");
                        info.put(key.toString(), sanitize(value));
                    }
                    continue;

                case AppealDate:
                    regex = months + ".+";
                    m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                    if (m.find()) {
                        info.put(key.toString(), sanitize(m.group()));
                    }
                    continue;

                case Gap_days:
                    // done later
                    continue;

                case ModeOfConviction:
                    if (!criminal) continue;
                    regex = "plea\\s*of\\s*guilty|jury\\s*verdict|nonjury\\s*trial";
                    m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                    if (m.find()) {
                        info.put(key.toString(), sanitize(m.group()));
                    }
                    continue;

                case Crimes:
                    // done later together with mode of conviction
                    continue;

                case Judges:
                    value = "";
                    regex = "Present—\\s*";
                    m = Pattern.compile(regex).matcher(text);
                    if (m.find()) {
                        value = m.group();
                        value = value.substring("Present—".length());
                        info.put(key.toString(), sanitize(value));
                        ++stats.judges;
                    }
                    if (value.isEmpty()) {
                        regex = "Present:\\s*";
                        m = Pattern.compile(regex).matcher(text);
                        if (m.find()) {
                            value = m.group();
                            value = value.substring("Present—".length());
                            info.put(key.toString(), sanitize(value));
                            ++stats.judges;
                        }
                    }
                    continue;

                case Defense:
                    regex = "";
                    // TODO
                    continue;

                case DefendantAppellant:
                    regex = "petitioners-plaintiffs-appellants";
                    m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                    if (m.find()) {
                        info.put(key.toString(), sanitize(m.group()));
                    }
                    continue;

                case DefendantRespondent:
                    regex = "respondents-defendants-respondents";
                    m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                    if (m.find()) {
                        info.put(key.toString(), sanitize(m.group()));
                    }
                    continue;

                case DistrictAttorney:
                    if (!criminal) continue;
                    regex = "\\n[a-zA-Z,.\\s]+District Attorney";
                    value = "";
                    m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                    if (m.find()) {
                        value = m.group().substring(2);
                        value = value.substring(0,  value.length() - "District Attorney".length());
                        value = sanitize(value);
                        info.put(key.toString(), value);
                    }
                    if (value.isEmpty()) {
                        ++stats.districtAttorneyProblem;
                    }
                    continue;

                case HarmlessError:
                    // TODO
                    continue;

                case NotHarmlessError:
                    // TODO
                    continue;

                default:
                    logger.error("Aren't you forgetting something, Mr.? How about {} field?", key.toString());

            }


        }
        boolean gapParsed = false;
        if (info.containsKey(KEYS.FirstDate.toString()) && info.containsKey(KEYS.AppealDate.toString())) {
            String firstDateStr = info.get(KEYS.FirstDate.toString());
            String appealDateStr = info.get(KEYS.AppealDate.toString());
            Date firstDate = null;
            Date appealDate = null;
            if (!firstDateStr.isEmpty()) {
                try {
                    firstDate = dateFormat.parse(firstDateStr);
                } catch (NumberFormatException | ParseException e) {
                    logger.error("Date parsing error for {}", firstDateStr);
                }
            }
            if (!appealDateStr.isEmpty()) {
                try {
                    appealDate = dateFormat.parse(appealDateStr);
                } catch (NumberFormatException | ParseException e) {
                    logger.error("Date parsing error for {}", appealDateStr);
                }
            }
            if (firstDate != null && appealDate != null) {
                long gap = appealDate.getTime() - firstDate.getTime();
                int gapDays = (int) (gap / 1000 / 60 / 60 / 24);
                if (gapDays > 0) {
                    info.put(KEYS.Gap_days.toString(), Integer.toString(gapDays));
                    gapParsed = true;
                }
            }
        }
        if (!gapParsed) ++stats.gapDays;
        if (criminal && info.containsKey(KEYS.ModeOfConviction.toString())) {
            String mode = info.get(KEYS.ModeOfConviction.toString());
            // crime is from here till the end of the line
            value = "";
            int crimeStart = text.indexOf(mode);
            if (crimeStart > 0) {
                crimeStart += mode.length();
                int comma = text.indexOf("\n", crimeStart);
                if (comma > 0 && (comma - crimeStart < 5)) crimeStart += (comma - crimeStart + 1);
                int crimeEnd = text.indexOf(".", crimeStart);
                if (crimeEnd > 0) {
                    value = text.substring(crimeStart, crimeEnd);
                    value = sanitize(value);
                    info.put(KEYS.Crimes.toString(), value);
                    ++stats.crimes;
                }
            }
            if (value.isEmpty() && sexOffender) {
                value = "risk pursuant to Sex Offender Registration Act";
                info.put(KEYS.Crimes.toString(), value);
                ++stats.crimes;
            }
        }
        if (info.containsKey(KEYS.DefendantAppellant.toString())) {
            // the answer is in the previous line
            value = info.get(KEYS.DefendantAppellant.toString());
            int valueInd = text.indexOf(value);
            if (valueInd > 0) {
                int endLine = text.lastIndexOf("\n", valueInd);
                if (endLine > 0) {
                    int startLine = text.lastIndexOf("\n", endLine - 1);
                    if (startLine > 0) {
                        value = text.substring(startLine + 1, endLine);
                        value = sanitize(value);
                        info.put(KEYS.DefendantAppellant.toString(), value);
                    }
                }
            }
        }
        if (info.containsKey(KEYS.DefendantRespondent.toString())) {
            // the answer is in the previous line
            value = info.get(KEYS.DefendantRespondent.toString());
            int valueInd = text.indexOf(value);
            if (valueInd > 0) {
                int endLine = text.lastIndexOf("\n", valueInd);
                if (endLine > 0) {
                    int startLine = text.lastIndexOf("\n", endLine - 1);
                    if (startLine > 0) {
                        value = text.substring(startLine + 1, endLine);
                        value = sanitize(value);
                        info.put(KEYS.DefendantRespondent.toString(), value);
                    }
                }
            }
        }
        return info;
    }

    public static void main(String[] args) {
        formOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("NYAppealParse - extract legal information from court cases", options);
            return;
        }
        NYAppealParse instance = new NYAppealParse();
        try {
            if (!instance.parseOptions(args)) {
                return;
            }
            instance.parseDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print(instance.stats.toString());
    }

    private static void formOptions() {
        options = new Options();
        options.addOption("i", "inputDir", true, "Input directory");
        options.addOption("o", "outputFile", true, "Output file, .csv will be added");
        options.addOption("b", "breakSize", true, "Output file size in lines");
    }

    private void parseDocuments() throws IOException {
        cleanupFirst();
        int lineCount = 0;
        writeHeader();
        File[] files = new File(inputDir).listFiles();
        Arrays.sort(files);
        stats.filesInDir = files.length;
        for (File file : files) {
            try {
                // right now, we analyze only "txt", and consider the rest as garbage
                if (!file.getName().endsWith("txt")) continue;
                ++stats.docs;
                StringBuffer buf = new StringBuffer();
                Map<String, String> answer = extractInfo(file);
                for (int e = 0; e < KEYS.values().length; ++e) {
                    String key = KEYS.values()[e].toString();
                    String value = "";
                    if (answer.containsKey(key)) {
                        value = answer.get(key);
                    }
                    buf.append(value).append(separator);
                }
                buf.deleteCharAt(buf.length() - 1);
                buf.append("\n");
                FileUtils.write(new File(outputFile + fileNumber + ".csv"), buf.toString(), true);
                ++stats.metadata;
                ++lineCount;
                if (lineCount >= breakSize) {
                    ++fileNumber;
                    lineCount = 1;
                    writeHeader();
                }
                buf = new StringBuffer();
                for (int e = 0; e < KEYS.values().length; ++e) {
                    String key = KEYS.values()[e].toString();
                    buf.append(key).append(separator);
                }
            } catch (IOException e) {
                logger.error("Error processing file {} " + file.getName());
            }
        }
    }

    private boolean parseOptions(String[] args) throws org.apache.commons.cli.ParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        inputDir = cmd.getOptionValue("inputDir");
        outputFile = cmd.getOptionValue("outputFile");
        if (cmd.hasOption("breakSize")) {
            breakSize = Integer.parseInt(cmd.getOptionValue("breakSize"));
        }
        return true;
    }

    private String sanitize(String value) {
        // remove all random occurrences of the separator
        value = value.replaceAll("\\" + separator, "");
        // limit the length
        if (value.length() > MAX_FIELD_LENGTH) value = value.substring(0, MAX_FIELD_LENGTH - 1) + "...";
        // take out new lines
        value = value.replaceAll("\\r\\n|\\r|\\n", " ");
        value = value.trim();
        if (value.endsWith(",")) value = value.substring(0, value.length() - 1);
        return value;
    }

    private void cleanupFirst() {
        File[] files = new File(outputFile).getParentFile().listFiles();
        for (File file : files) {
            if (file.getName().endsWith("csv")) file.delete();
        }
    }

    private void writeHeader() throws IOException {
        StringBuffer buf = new StringBuffer();
        for (int e = 0; e < KEYS.values().length; ++e) {
            String key = KEYS.values()[e].toString();
            buf.append(key).append(separator);
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append("\n");
        // create new file, append = false
        FileUtils.write(new File(outputFile + fileNumber + ".csv"), buf.toString(), false);
    }

    private String betweenTheLions(String text, char lion1, char lion2) {
        String regex = "\\" + lion1 + ".+" + "\\" + lion2;
        Matcher m = Pattern.compile(regex).matcher(text);
        if (m.find()) {
            String value = m.group();
            return value.substring(1, value.length() - 1);
        } else {
            return text;
        }
    }

    private String findAll(String text, String [] expr) {
        StringBuilder results = new StringBuilder();
        for (String regex: expr) {
            Matcher m = Pattern.compile(regex).matcher(text);
            if (m.find()) results.append(sanitize(m.group())).append(";");
        }
        if (results.length() > 0) results.deleteCharAt(results.length() - 1);
        return results.toString();
    }
}


