package com.hyperiongray.court;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NYAppealParse {
    private static final Logger logger = LoggerFactory.getLogger(NYAppealParse.class);
    private static Options options;

    public enum KEYS {
        File, Casenumber, CivilKriminal, Court, County, Judge, DistrictAttorney, ADA, // assistant district attorney
        Keywords, GroundsForAppeal, FirstDate, AppealDate,
        Gap_days, ModeOfConviction, Crimes, Judges, Defense, DefendantAppellant, DefendantRespondent,
        HarmlessError, ProsecutMisconduct, DocumentLength
        //, SexOffender
    }

    private final static int MAX_FIELD_LENGTH = 100; // more than that is probably a bug, so don't make it a parameter
    private Stats stats = new Stats();

    private String inputDir;
    private String outputFile;
    private int breakSize = 10000;
    private char separator = '|';
    private String months = "(January|February|March|April|May|June|July|August|September|October|November|December)";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMdd,yyyy");

    Pattern[] keywords = {Pattern.compile("unanimously\\s*affirmed|affirmed"),
            Pattern.compile("unanimously\\s*modified|modified"),
            Pattern.compile("unanimously\\s*reversed|reversed"),
            Pattern.compile("unanimously\\s*dismissed|dismissed"),
            Pattern.compile("cases*is\\s*held"),
            Pattern.compile("decisions*is\\s*reserved"),
            Pattern.compile("matters*is\\s*remitted")};
    Pattern[] grounds = {
            Pattern.compile("sufficient"), Pattern.compile("speedy"), Pattern.compile("suppress"), Pattern.compile("sex offense registry"),
            Pattern.compile("double jeopardy"), Pattern.compile("ineffective counsel"),
            Pattern.compile("coerce"), Pattern.compile("coercion"), Pattern.compile("incapac"), Pattern.compile("mental"),
            Pattern.compile("resentenc"), Pattern.compile("sever"), Pattern.compile("youth"), Pattern.compile("juror"), Pattern.compile("instructions")
    };

    Pattern[] defense = {
            Pattern.compile("public\\s*defender", Pattern.CASE_INSENSITIVE),
            Pattern.compile("conflict\\s*defender", Pattern.CASE_INSENSITIVE),
            Pattern.compile("legal\\s*aid\\s*BUREAU", Pattern.CASE_INSENSITIVE),
            Pattern.compile("legal\\s*aid\\s*SOCIETY", Pattern.CASE_INSENSITIVE)
    };
    boolean sexOffender = false;
    String sexOffenderKeywords = "sex\\s*offender\\s*registration\\s*act";


    private Pattern CRIMINAL_PATTERN = Pattern.compile("People v ", Pattern.CASE_INSENSITIVE);
    private Pattern SEX_OFFENDER_PATTERN = Pattern.compile("sex\\s*offender\\s*registration\\s*act", Pattern.CASE_INSENSITIVE);

    private Pattern CASE_NUMBER_1_PATTERN = Pattern.compile("\\[[0-9]+\\sAD.+\\]", Pattern.CASE_INSENSITIVE);
    private Pattern CASE_NUMBER_2_PATTERN = Pattern.compile("[0-9]{4}.+NY.*Slip.*Op.*[0-9]+", Pattern.CASE_INSENSITIVE);

    private String COURT_REGEXP = "(Supreme Court)|(County Court)|(Court of Claims)|(Family Court)|" +
            "(Workers' Compensation Board)|(Division of Human Rights)|" +
            "(Unemployment Insurance Appeal Board)|(Department of Motor Vehicles)";
    private Pattern COURT_1_PATTERN = Pattern.compile(COURT_REGEXP, Pattern.CASE_INSENSITIVE);
    private Pattern COURT_COMMITTEE_PATTERN = Pattern.compile("\\s+[a-zA-Z]+\\s+Committee\\s+[a-zA-Z\\s]+");

    private Pattern COUNTRY_PATTERN = Pattern.compile("[a-zA-Z]+\\sCounty", Pattern.CASE_INSENSITIVE);

    private Pattern JUDGE_PATTERN = Pattern.compile("(Court|County|Court of Claims) \\(.+?\\)");
    private Pattern IN_PARENTHESES_PATTERN = Pattern.compile("\\(.+\\)");

    private Pattern FIRST_DATE_PATTERN = Pattern.compile("(rendered|entered|dated|filed|imposed|entered on or about) " + months + " [0-9]+?, [1-2][0-9][0-9][0-9]", Pattern.CASE_INSENSITIVE);
    private Pattern APPEAL_DATE_PATTERN = Pattern.compile(months + "\\s[0-9]+,\\s[0-9]+", Pattern.CASE_INSENSITIVE);

    private Pattern CONVICTION_PATTERN = Pattern.compile("plea\\s*of\\s*guilty|jury\\s*verdict|nonjury\\s*trial", Pattern.CASE_INSENSITIVE);

    private Pattern JUDGES_1_PATTERN = Pattern.compile("Present[–—:].+", Pattern.CASE_INSENSITIVE);
    private Pattern JUDGES_2_PATTERN = Pattern.compile(".+concur\\.", Pattern.CASE_INSENSITIVE);

    private final String DEF_APP = "defendant-appellant";
    private Pattern DEFENDANT_APPELLANT_PATTERN = Pattern.compile(DEF_APP + "[s]?", Pattern.CASE_INSENSITIVE);
    private Pattern DEFENDANT_RESPONDENT_PATTERN = Pattern.compile("defendant-respondent[s]?", Pattern.CASE_INSENSITIVE);

    private Pattern DA_1_PATTERN = Pattern.compile("\\n[a-zA-Z,.\\s]*District Attorney", Pattern.CASE_INSENSITIVE);
    private Pattern DA_2_PATTERN = Pattern.compile("\\([a-zA-Z,\\.\\s]+of\\s+counsel[\\);]", Pattern.CASE_INSENSITIVE);

    private Pattern HARMLESS_ERROR_PATTERN = Pattern.compile("([^.]*?harmless[^.]*\\.)", Pattern.CASE_INSENSITIVE);

    private Pattern PROSECUTOR_MISCONDUCT_PATTERN = Pattern.compile("prosecut[a-zA-Z\\s]*misconduct", Pattern.CASE_INSENSITIVE);
    // END OF compiled PATTERNs

    public Map<String, String> extractInfo(File file) throws IOException {
        String text = FileUtils.readFileToString(file);
        String textFlow = text.replaceAll("\\r\\n|\\r|\\n", " ");

        //System.out.println("Text flow: " + textFlow);
        Map<String, String> info = new HashMap<>();
        // there are so many exceptions that 'case' is preferable to a generic loops with exceptions
        Matcher m;
        String value = "";
        // civil vs criminal
        boolean criminal;

        m = CRIMINAL_PATTERN.matcher(text);
        criminal = m.find();
        if (criminal) {
            // this should occur almost in the beginning of the file
            if (text.indexOf("People v ") > 100) {
                criminal = false;
            }
        }
        // sex offender
        m = SEX_OFFENDER_PATTERN.matcher(text);
        sexOffender = m.find();
        if (sexOffender) criminal = true;

        for (int e = 0; e < KEYS.values().length; ++e) {
            KEYS key = KEYS.values()[e];
            value = "";

            // put in a placeholder value - unless something was already parsed together with a different key, out of order
            if (!info.containsKey(key.toString())) {
                info.put(key.toString(), value);
            }

            switch (key) {
                case File:
                    info.put(KEYS.File.toString(), file.getName());
                    continue;
                case Casenumber:
                    value = "";
                    m = CASE_NUMBER_1_PATTERN.matcher(text);
                    if (m.find()) {
                        value = sanitize(m.group());
                        if (value.length() >= 3 && value.length() <= 15 && value.contains("AD")) {
                            info.put(key.toString(), value);
                        }
                    }
                    if (value.isEmpty()) {
                        m = CASE_NUMBER_2_PATTERN.matcher(text);
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

//                case SexOffender:
//                    info.put(KEYS.SexOffender.toString(), sexOffender? "Y" : "");
//                    if (sexOffender) {
//                        ++stats.sexOffence;
//                    }
//                    continue;

                case DocumentLength:
                    info.put(KEYS.DocumentLength.toString(), Integer.toString(text.length()));
                    continue;
                case Court:
                    value = "";
                    //  regex = "(Supreme Court)|(County Court)|(Court of Claims)|(Family Court)|" +
                    //          "(Workers' Compensation Board)|(Division of Human Rights)|" +
                    //          "(Unemployment Insurance Appeal Board)|(Department of Motor Vehicles)";
                    m = COURT_1_PATTERN.matcher(text);
                    if (m.find()) {
                        value = m.group();
                        info.put(key.toString(), sanitize(value));
                    }
                    if (value.isEmpty()) {
                        //regex = "\\s+[a-zA-Z]+\\s+Committee\\s+[a-zA-Z\\s]+";
                        m = COURT_COMMITTEE_PATTERN.matcher(textFlow);
                        if (m.find()) {
                            value = m.group();
                            value = value.substring(1);
                            info.put(key.toString(), sanitize(value));
                        }
                        info.put(key.toString(), value);
                    }
                    if (!value.isEmpty()) ++stats.court;
                    if (value.isEmpty()) logger.debug("Court problem in file {} ", file.getName());
                    continue;
                case County:
                    value = "";
                    //regex = "[a-zA-Z]+\\sCounty";
                    m = COUNTRY_PATTERN.matcher(text);
                    if (m.find()) {
                        value = sanitize(m.group());
                        // county is found further down, but clos nearby
                        if (value.toLowerCase().equals("the county")) {
                            if (m.find()) {
                                value = sanitize((m.group()));
                            }
                        }
                        // this is a quotation, and county should have been found earlier or not at all
                        if (value.toLowerCase().equals("v county")) {
                            value = "";
                        }

                        info.put(key.toString(), value);
                        ++stats.county;
                    }
                    continue;

                case Judge:
                    m = JUDGE_PATTERN.matcher(textFlow);
                    if (m.find()) {
                        value = m.group();
                        value = inParentheses(value);
                        info.put(key.toString(), sanitize(value));
                        ++stats.judge;
                    } else {
                        value = "";
                    }
                    continue;

                case Keywords:
                    value = findAll(text, keywords);
                    info.put(key.toString(), value);
                    if (!value.isEmpty()) ++stats.keywords;
                    continue;

                case GroundsForAppeal:
                    value = findAll(text, grounds);
                    info.put(key.toString(), value);
                    continue;

                case FirstDate:
                    value = "";
                    //regex = "(rendered|entered|dated|filed) " + months + " [0-9]+?, 2[0-1][0-9][0-9]";
                    m = FIRST_DATE_PATTERN.matcher(textFlow);
                    if (m.find()) {
                        value = m.group();
                        value = value.replaceAll("[Rr]endered ", "");
                        value = value.replaceAll("(Dated|dated|DATED) ", "");
                        value = value.replaceAll("[Ff]iled ", "");
                        value = value.replaceAll("[Ii]mposed ", "");
                        value = value.replaceAll("[Ee]ntered on or about ", "");
                        value = value.replaceAll("[Ee]ntered ", "");

                        value = sanitize(value);
                        info.put(key.toString(), value);
                    }
                    if (!value.isEmpty()) ++stats.firstDate;
                    if (value.isEmpty()) {
                        logger.warn("First date parsing error in {}", file.getName());
                    }
                    continue;

                case AppealDate:
                    value = "";
                    //regex = months + "\\s[0-9]+,\\s[0-9]+";
                    m = APPEAL_DATE_PATTERN.matcher(text);
                    if (m.find()) {
                        value = sanitize(m.group());
                        info.put(key.toString(), value);
                    }
                    if (!value.isEmpty()) ++stats.appealDate;
                    continue;

                case Gap_days:
                    // done later
                    continue;

                case ModeOfConviction:
                    if (!criminal) continue;
                    value = "";
                    //regex = "plea\\s*of\\s*guilty|jury\\s*verdict|nonjury\\s*trial";
                    m = CONVICTION_PATTERN.matcher(text);
                    if (m.find()) {
                        value = sanitize(m.group());
                        info.put(key.toString(), value);
                    }
                    if (!value.isEmpty()) ++stats.modeOfConviction;
                    if (value.isEmpty()) {
                        logger.warn("Problem with mode of conviction in {}", file.getName());
                    }
                    continue;

                case Crimes:
                    // done later together with mode of conviction
                    continue;

                case Judges:
                    value = "";
                    // includes a special dash
                    m = JUDGES_1_PATTERN.matcher(textFlow);
                    if (m.find()) {
                        value = m.group();
                        value = value.substring("Present".length() + 1);
                        info.put(key.toString(), sanitize(value));
                        ++stats.judges;
                    }
                    if (value.isEmpty()) {
                        m = JUDGES_2_PATTERN.matcher(text);
                        if (m.find()) {
                            // hack, could not do it with regex
                            value = m.group();
                            value = value.substring(0, value.length() - "concur.".length());
                            value = sanitize(value);
                        }
                        value = sanitize(value);
                        info.put(key.toString(), value);
                        ++stats.judges;
                    }
                    continue;

                case Defense:
                    value = findAll(text, defense);
                    info.put(key.toString(), value);
                    continue;

                case DefendantAppellant:;
                    m = DEFENDANT_APPELLANT_PATTERN.matcher(text);
//                    if (m.find()) {
//                        value = m.group();
//                        if (value.equals(DEF_APP)) {
//                            // take the previous line
//                            int defAppIndex = text.indexOf(DEF_APP);
//                            printAround(text, defAppIndex);
//                            if (defAppIndex >= 0) {
//                                int indexBefore = text.lastIndexOf("\n", defAppIndex);
//                                printAround(text, indexBefore);
//                                if (indexBefore > 0) {
//                                    int indexBeforeBefore = text.lastIndexOf("\n", indexBefore - 1);
//                                    if (indexBeforeBefore >= 0) {
//                                        value = text.substring(indexBeforeBefore, indexBefore);
//                                        printAround(text, indexBeforeBefore);
//                                    }
//                                }
//                            }
//                         }
//                        info.put(key.toString(), sanitize(value));
//                    }
                    if (m.find()) {
                        info.put(key.toString(), "1");
                    } else {
                        info.put(key.toString(), "0");
                    }
                    continue;

                case DefendantRespondent:
                    m = DEFENDANT_RESPONDENT_PATTERN.matcher(text);
                    if (m.find()) {
                        info.put(key.toString(), "1");
                    } else {
                        info.put(key.toString(), "0");
                    }
                    continue;

                case DistrictAttorney:
                    if (!criminal) continue;
                    m = DA_1_PATTERN.matcher(text);
                    if (m.find()) {
                        value = m.group().substring(2);
                        value = value.substring(0, value.length() - "District Attorney".length());
                        value = sanitize(value);
                        info.put(key.toString(), value);
                    }
                    value = "";

                    if (value.isEmpty()) {
                        ++stats.districtAttorneyProblem;
                    } else {
                        // also find ADA, which is next to DA, in parenthesis
                        int index = text.toLowerCase().indexOf("district attorney");
                        if (index > 0) {
                            m = DA_2_PATTERN.matcher(textFlow.substring(index));
                            if (m.find()) {
                                value = m.group();
                                value = inParentheses(value);
                                value = value.substring(0, value.length() - "of counsel".length());
                                value = sanitize(value);
                                info.put(KEYS.ADA.toString(), value);
                            }
                        }
                    }
                    continue;

                case ADA:
                    // just done above, together with District Attorney
                    continue;

                case HarmlessError:
                    m = HARMLESS_ERROR_PATTERN.matcher(text);
                    if (m.find()) {
                        info.put(key.toString(), sanitize(m.group()));
                    }

                    continue;

                case ProsecutMisconduct:
                    //regex = "prosecut[a-zA-Z\\s]*misconduct";
                    m = PROSECUTOR_MISCONDUCT_PATTERN.matcher(text);
                    if (m.find()) {
                        info.put(key.toString(), sanitize(m.group()));
                    }
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
                    logger.error("Date parsing error for {} in {}", firstDateStr, file.getName());
                }
            }
            if (!appealDateStr.isEmpty()) {
                try {
                    appealDate = dateFormat.parse(appealDateStr);
                } catch (NumberFormatException | ParseException e) {
                    logger.error("Date parsing error for {} in {}", appealDateStr, file.getName());
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
        if (gapParsed) ++stats.gapDays;
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
//        if (info.containsKey(KEYS.DefendantAppellant.toString())) {
//            // the answer is in the previous line
//            value = info.get(KEYS.DefendantAppellant.toString());
//            int valueInd = text.indexOf(value);
//            if (valueInd > 0) {
//                int endLine = text.lastIndexOf("\n", valueInd);
//                if (endLine > 0) {
//                    int startLine = text.lastIndexOf("\n", endLine - 1);
//                    if (startLine > 0) {
//                        value = text.substring(startLine + 1, endLine);
//                        value = sanitize(value);
//                        info.put(KEYS.DefendantAppellant.toString(), value);
//                    }
//                }
//            }
//        }
//        if (info.containsKey(KEYS.DefendantRespondent.toString())) {
//            // the answer is in the previous line
//            value = info.get(KEYS.DefendantRespondent.toString());
//            int valueInd = text.indexOf(value);
//            if (valueInd > 0) {
//                int endLine = text.lastIndexOf("\n", valueInd);
//                if (endLine > 0) {
//                    int startLine = text.lastIndexOf("\n", endLine - 1);
//                    if (startLine > 0) {
//                        value = text.substring(startLine + 1, endLine);
//                        value = sanitize(value);
//                        info.put(KEYS.DefendantRespondent.toString(), value);
//                    }
//                }
//            }
//        }
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
                FileUtils.write(new File(outputFile + stats.fileNumber + ".csv"), buf.toString(), true);
                ++stats.metadata;
                ++lineCount;
                if (lineCount >= breakSize) {
                    buf = new StringBuffer();
                    ++stats.fileNumber;
                    lineCount = 1;
                    writeHeader();
                    System.out.println("Writing parsed file " + stats.fileNumber);
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
        FileUtils.write(new File(outputFile + stats.fileNumber + ".csv"), buf.toString(), false);
    }

    private String inParentheses(String text) {
        Matcher m = IN_PARENTHESES_PATTERN.matcher(text);
        if (m.find()) {
            String value = m.group();
            return value.substring(1, value.length() - 1);
        } else {
            return text;
        }
    }

    private String findAll(String text, Pattern[] patterns) {
        StringBuilder results = new StringBuilder();
        for (Pattern pattern : patterns) {
            Matcher m = pattern.matcher(text);
            if (m.find()) results.append(sanitize(m.group())).append(";");
        }
        if (results.length() > 0) results.deleteCharAt(results.length() - 1);
        return results.toString();
    }

    private String readBackToLine(String text, int fromIndex) {
        return readBackToChar(text, fromIndex, '\n');
    }

    private String readBackToChar(String text, int fromIndex, int breakCh) {
        int firstIndex = readBackAndReturnIndex(text, fromIndex, breakCh);
        return text.substring(firstIndex + 1, fromIndex);
    }

    private int readBackAndReturnIndex(String text, int fromIndex, int breakCh) {
        int firstIndex = fromIndex;
        int ch = text.charAt(firstIndex);
        while (ch != breakCh) {
            firstIndex--;
            ch = text.charAt(firstIndex);
        }
        return firstIndex;
    }
    // helper debug function
    private void printAround(String str, int index) {
        int circle = 20;
        int start = index > circle ? index - circle : 0;
        int end = str.length() > index + circle ? index + circle : str.length();
        System.out.println(str.substring(start, end));
    }
}


