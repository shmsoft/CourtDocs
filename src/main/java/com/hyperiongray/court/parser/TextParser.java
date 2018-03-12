package com.hyperiongray.court.parser;

import com.hyperiongray.court.CommonConstants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser implements IParser {
    private static final Logger logger =
            LoggerFactory.getLogger(TextParser.class);

    private final static int MAX_FIELD_LENGTH = 100;
    // more than that is probably a bug, so don't make it a parameter
    public static final String PARSER_TYPE = "text";
    public static final String COMMON_STR_REGX = "The People of the State of New York, (Appellant|Respondent|Defendant Appellant), v";
    private Stats stats = new Stats();

    private String months =
            "(January|February|March|April|May|June|July|August|September|October|November|December)";
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMMMMdd,yyyy");

    Pattern[] keywords =
            {Pattern.compile("affirmed"),
                    Pattern.compile("modified"),
                    Pattern.compile("reversed"),
                    Pattern.compile("dismissed"),
                    Pattern.compile("cases*is\\s*held"),
                    Pattern.compile("decisions*is\\s*reserved"),
                    Pattern.compile("matters*is\\s*remitted"),
                    Pattern.compile("interest of justice"),
                    Pattern.compile("waiver of appeal"),
                    Pattern.compile("plea")
            };
    Pattern[] grounds =
            {Pattern.compile("sufficient"), Pattern.compile("speedy"),
                    Pattern.compile("suppress"), Pattern.compile("sex offense registry"),
                    Pattern.compile("double jeopardy"),
                    Pattern.compile("ineffective counsel"), Pattern.compile("coerce"),
                    Pattern.compile("coercion"), Pattern.compile("incapac"),
                    Pattern.compile("mental"), Pattern.compile("resentenc"),
                    Pattern.compile("sever"), Pattern.compile("youth"),
                    Pattern.compile("juror"), Pattern.compile("instructions")};

    Pattern[] defense =
            {Pattern.compile("public\\s*defender", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("conflict\\s*defender", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("legal\\s*aid\\s*BUREAU", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("legal\\s*aid\\s*SOCIETY",
                            Pattern.CASE_INSENSITIVE)};

    Pattern[] unanimous =
            {Pattern.compile("unanimous"), Pattern.compile("unanimously")};

    Pattern[] justice = {Pattern.compile("interest\\s*of\\s*justice",
            Pattern.CASE_INSENSITIVE)};

    boolean sexOffender = false;
    String sexOffenderKeywords = "sex\\s*offender\\s*registration\\s*act";

    private Pattern CRIMINAL_PATTERN =
            Pattern.compile("People v ", Pattern.CASE_INSENSITIVE);
    private Pattern SEX_OFFENDER_PATTERN = Pattern
            .compile("sex\\s*offender\\s*registration\\s*act",
                    Pattern.CASE_INSENSITIVE);

    private Pattern CASE_NUMBER_1_PATTERN =
            Pattern.compile("\\[[0-9]+\\sAD.+\\]", Pattern.CASE_INSENSITIVE);
    private Pattern CASE_NUMBER_2_PATTERN = Pattern
            .compile("[0-9]{4}.+NY.*Slip.*Op.*[0-9]+", Pattern.CASE_INSENSITIVE);

    private String COURT_REGEXP =
            "(Supreme Court)|(County Court)|(Court of Claims)|(Family Court)|"
                    + "(Workers' Compensation Board)|(Division of Human Rights)|"
                    + "(Unemployment Insurance Appeal Board)|(Department of Motor Vehicles)";
    private Pattern COURT_1_PATTERN =
            Pattern.compile(COURT_REGEXP, Pattern.CASE_INSENSITIVE);
    private Pattern COURT_COMMITTEE_PATTERN =
            Pattern.compile("\\s+[a-zA-Z]+\\s+Committee\\s+[a-zA-Z\\s]+");

    private Pattern COUNTRY_PATTERN =
            Pattern.compile("[a-zA-Z]+\\sCounty", Pattern.CASE_INSENSITIVE);

    private Pattern JUDGE_PATTERN =
            Pattern.compile("(Court|County|Court of Claims) \\(.+?\\)");
    private Pattern IN_PARENTHESES_PATTERN = Pattern.compile("\\(.+\\)");
    public static final String APPELLANT_PATTERN_STR = "The People of the State of New York, (Appellant|Respondent|Defendant Appellant), .* (Appellant|Respondent|Defendant Appellant).";
    private final Pattern APPELLANT = Pattern.compile(APPELLANT_PATTERN_STR);

    //rendered on or about October 26, 2007
    //Judgment, Supreme Court, Bronx County (William Mogulescu, J.), rendered on or about October 26, 2007, unanimously affirmed.

    private Pattern FIRST_DATE_PATTERN = Pattern.compile(
            "(rendered|entered|dated|filed|imposed|entered)( on or about)? " + months
                    + " ([0-9]+?, [1-2][0-9][0-9][0-9])", Pattern.CASE_INSENSITIVE);
    private Pattern APPEAL_DATE_PATTERN =
            Pattern.compile(months + "\\s[0-9]+,\\s[0-9]+", Pattern.CASE_INSENSITIVE);

    private Pattern CONVICTION_PATTERN = Pattern
            .compile("plea\\s*of\\s*guilty|jury\\s*verdict|nonjury\\s*trial",
                    Pattern.CASE_INSENSITIVE);

    private Pattern CRIMES_PATTERN_1 = Pattern
            .compile("upon\\s(his|her)\\splea\\sof\\sguilty,\\sof\\s(.*)\\.",
                    Pattern.CASE_INSENSITIVE);
    // TODO instead of him/his it should probably be him/her
    private Pattern CRIMES_PATTERN_2 = Pattern
            .compile("convicting\\s(him|his)\\sof\\s(.*),\\supon",
                    Pattern.CASE_INSENSITIVE);

    private Pattern JUDGES_1_PATTERN =
            Pattern.compile("^(Present[–—:]).+", Pattern.CASE_INSENSITIVE);
    private Pattern JUDGES_2_PATTERN =
            Pattern.compile(".+concur\\.", Pattern.CASE_INSENSITIVE);
    private Pattern JUDGES_3_PATTERN =
            Pattern.compile("^Concur[-—](.*)$", Pattern.CASE_INSENSITIVE);

    private final String DEF_APP = "New York, (respondent|Respondent)";
    private Pattern DEFENDANT_APPELLANT_PATTERN =
            Pattern.compile(DEF_APP + "[s]?", Pattern.CASE_INSENSITIVE);
    private Pattern DEFENDANT_RESPONDENT_PATTERN =
            Pattern.compile("defendant-respondent[s]?", Pattern.CASE_INSENSITIVE);

    private Pattern DA_1_PATTERN =
            Pattern.compile("(.*)\\sDistrict Attorney", Pattern.CASE_INSENSITIVE);
    private Pattern DA_2_PATTERN = Pattern
            .compile("\\([a-zA-Z,\'\\.\\s]+of\\s+counsel[\\);]",
                    Pattern.CASE_INSENSITIVE);

    private Pattern HARMLESS_ERROR_PATTERN =
            Pattern.compile("([^.]*?harmless[^.]*\\.)", Pattern.CASE_INSENSITIVE);

    private Pattern PROSECUTOR_MISCONDUCT_PATTERN = Pattern
            .compile("prosecut[a-zA-Z\\s]*misconduct", Pattern.CASE_INSENSITIVE);
    // END OF compiled PATTERNs

    public Stats getStats() {
        return stats;
    }

    public Map<DataKey, String> parseFile(File file) throws IOException {
        String text = FileUtils.readFileToString(file);
        text = text.replaceAll("" + CommonConstants.CSV_FIELD_SEPARATOR, "");
        String textFlow = text.replaceAll("\\r\\n|\\r|\\n", " ");

        //System.out.println("Text flow: " + textFlow);
        Map<DataKey, String> info = new HashMap<>();
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
        if (sexOffender) {
            criminal = true;
        }

        for (int e = 0; e < DataKey.values().length; ++e) {
            DataKey key = DataKey.values()[e];
            value = "";

            // put in a placeholder value - unless something was already parsed together with a different key, out of order
            if (!info.containsKey(key)) {
                info.put(key, value);
            }

            switch (key) {
                case File:
                    info.put(DataKey.File, file.getName());
                    continue;
                case CaseName:
                    info.put(DataKey.CaseName, "");
                    continue;
                case Casenumber:
                    value = "";
                    m = CASE_NUMBER_1_PATTERN.matcher(text);
                    if (m.find()) {
                        value = sanitize(m.group());
                        if (value.length() >= 3 && value.length() <= 15 && value
                                .contains("AD")) {
                            info.put(key, value);
                        }
                    }
                    if (value.isEmpty()) {
                        m = CASE_NUMBER_2_PATTERN.matcher(text);
                        if (m.find()) {
                            value = sanitize(m.group());
                            info.put(key, value);
                        }
                    }
                    if (!value.isEmpty()) {
                        stats.inc(DataKey.Casenumber);
                    }
                    continue;

                case CivilKriminal:
                    info.put(DataKey.CivilKriminal, criminal ? "K" : "C");
                    if (criminal) {
                        stats.inc(DataKey.Criminal);
                    } else {
                        stats.inc(DataKey.Civil);
                    }
                    continue;

                    //                case SexOffender:
                    //                    info.put(KEYS.SexOffender.toString(), sexOffender? "Y" : "");
                    //                    if (sexOffender) {
                    //                        ++stats.sexOffence;
                    //                    }
                    //                    continue;

                case DocumentLength:
                    info.put(DataKey.DocumentLength, Integer.toString(text.length()));
                    continue;
                case Court:
                    value = "";
                    //  regex = "(Supreme Court)|(County Court)|(Court of Claims)|(Family Court)|" +
                    //          "(Workers' Compensation Board)|(Division of Human Rights)|" +
                    //          "(Unemployment Insurance Appeal Board)|(Department of Motor Vehicles)";
                    m = COURT_1_PATTERN.matcher(text);
                    if (m.find()) {
                        value = m.group();
                        info.put(key, sanitize(value));
                    }
                    if (value.isEmpty()) {
                        //regex = "\\s+[a-zA-Z]+\\s+Committee\\s+[a-zA-Z\\s]+";
                        m = COURT_COMMITTEE_PATTERN.matcher(textFlow);
                        if (m.find()) {
                            value = m.group();
                            value = value.substring(1);
                            info.put(key, sanitize(value));
                        }
                        info.put(key, value);
                    }
                    if (!value.isEmpty()) {
                        stats.inc(DataKey.Court);
                    }
                    if (value.isEmpty()) {
                        logger.debug("Court problem in file {} ", file.getName());
                    }
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

                        if (NYAppealUtil.isCounty(value)) {
                            info.put(key, value);
                            stats.inc(DataKey.County, value);
                        }
                    }
                    continue;

                case Judge:
                    m = JUDGE_PATTERN.matcher(textFlow);
                    if (m.find()) {
                        value = m.group();
                        value = inParentheses(value);
                        info.put(key, sanitize(value));
                        stats.inc(DataKey.Judge, value);
                    } else {
                        value = "";
                    }
                    continue;
                 case DefendantORAppellant:
                    m = APPELLANT.matcher(textFlow);
                    if (m.find()) {
                        value = m.group();
                        info.put(key, value.replaceAll(COMMON_STR_REGX, ""));
                    }
                    continue;
                case Keywords:
                    value = findAll(text, keywords);
                    info.put(key, value);
                    if (!value.isEmpty()) {
                        stats.inc(DataKey.Keywords, value);
                    }
                    continue;

                case GroundsForAppeal:
                    value = findAll(text, grounds);
                    info.put(key, value);
                    continue;

                case FirstDate:
                    value = "";
                    List<String> sentences = NYAppealUtil.splitToSentences(textFlow);
                    for (String sentence : sentences) {
                        //regex = "(rendered|entered|dated|filed) " + months + " [0-9]+?, 2[0-1][0-9][0-9]";
                        //Judgment, Supreme Court, Bronx County (William Mogulescu, J.), rendered on or about October 26, 2007, unanimously affirmed.
                        m = FIRST_DATE_PATTERN.matcher(sentence);
                        if (m.find()) {
                            value = m.group(3) + " " + m.group(4);
                            value = sanitize(value);
                            info.put(key, value);
                            break;
                        }
                    }
                    if (!value.isEmpty()) {
                        stats.inc(DataKey.FirstDate, value);
                    }
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
                        info.put(key, value);
                    }
                    if (!value.isEmpty()) {
                        stats.inc(DataKey.AppealDate, value);
                    }
                    continue;

                case Unanimous:
                    String results = findAll(text, unanimous);
                    if (!results.isEmpty()) {
                        info.put(key, "1");
                    } else {
                        info.put(key, "0");
                    }
                    continue;

                case Gap_days:
                    // done later
                    continue;

                case ModeOfConviction:
                    if (!criminal)
                        continue;
                    value = "";
                    //regex = "plea\\s*of\\s*guilty|jury\\s*verdict|nonjury\\s*trial";
                    m = CONVICTION_PATTERN.matcher(text);
                    if (m.find()) {
                        value = sanitize(m.group());
                        info.put(key, value);
                    }
                    if (!value.isEmpty()) {
                        stats.inc(DataKey.ModeOfConviction);
                    }
                    if (value.isEmpty()) {
                        logger.warn("Problem with mode of conviction in {}", file.getName());
                    }
                    continue;

                case Crimes:
                    //                	 if (criminal && info.containsKey(KEYS.ModeOfConviction.toString())) {
                    //                         String mode = info.get(KEYS.ModeOfConviction.toString());
                    //                         // crime is from here till the end of the line
                    //                         value = "";
                    //                         int crimeStart = text.indexOf(mode);
                    //                         if (crimeStart > 0) {
                    //                             crimeStart += mode.length();
                    //                             int comma = text.indexOf("\n", crimeStart);
                    //                             if (comma > 0 && (comma - crimeStart < 5)) crimeStart += (comma - crimeStart + 1);
                    //                             int crimeEnd = text.indexOf(".", crimeStart);
                    //                             if (crimeEnd > 0) {
                    //                                 value = text.substring(crimeStart, crimeEnd);
                    //                                 value = sanitize(value);
                    //                                 info.put(KEYS.Crimes.toString(), value);
                    //                                 ++stats.crimes;
                    //                             }
                    //                         }
                    //                         if (value.isEmpty() && sexOffender) {
                    //                         }
                    //                     }
                    if (criminal) {
                        m = CRIMES_PATTERN_1.matcher(textFlow);
                        if (m.find()) {
                            value = m.group(2);
                        } else {
                            m = CRIMES_PATTERN_2.matcher(textFlow);
                            if (m.find()) {
                                value = m.group(2);
                            }
                        }
                        if (!value.isEmpty() && value.contains(".")) {
                            value = value.split("\\.")[0];
                        }
                        if (value.isEmpty() && sexOffender) {
                            value = "risk pursuant to Sex Offender Registration Act";
                        }
                        if (!value.isEmpty()) {
                            info.put(DataKey.Crimes, value);
                            stats.inc(DataKey.Crimes);
                        }
                    }
                    continue;

                case Judges:
                    sentences = NYAppealUtil.splitToSentences(textFlow);
                    for (String sentence : sentences) {
                        if (sentence.contains(
                                "concur except")) { // Filter out 'All concur except .. '
                            continue;
                        }
                        m = JUDGES_1_PATTERN.matcher(sentence);
                        if (m.find()) {
                            value = sentence.substring(m.group(1).length());
                            break;
                        } else {
                            m = JUDGES_2_PATTERN.matcher(sentence);
                            if (m.find()) {
                                value =
                                        sentence.substring(0, sentence.length() - "concur.".length());
                                break;
                            } else {
                                m = JUDGES_3_PATTERN.matcher(sentence);
                                if (m.find()) {
                                    value = m.group(1);
                                    if (value.startsWith("-")) {
                                        value = value.substring(1);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (!value.isEmpty()) {
                        value = sanitize(value);
                        // occasionally list of judges ends with (Filed .. )
                        int idx = value.indexOf("(Filed");
                        if (idx >= 0) {
                            value = value.substring(0, idx);
                        }
                        info.put(key, value);
                        stats.inc(DataKey.Judges);
                    }
                    continue;

                case Defense:
                    value = findAll(text, defense);
                    info.put(key, value);
                    continue;

                case DefendantAppellant:
                    ;
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
                        info.put(key, "1");
                    } else {
                        info.put(key, "0");
                    }
                    continue;

                case DefendantRespondent:
                    m = DEFENDANT_RESPONDENT_PATTERN.matcher(text);
                    if (m.find()) {
                        info.put(key, "1");
                    } else {
                        info.put(key, "0");
                    }
                    continue;

                case DistrictAttorney:
                    if (!criminal) {
                        continue;
                    }
                    sentences = NYAppealUtil.splitToSentences(textFlow);
                    value = "";
                    for (String sentence : sentences) {
                        sentence = sanitize(sentence);
                        m = DA_1_PATTERN.matcher(sentence);
                        if (m.find()) {
                            value = m.group();
                            int end = value.length() - "District Attorney".length();
                            // TODO fix this hack
                            if (end < 0)
                                end = 0;
                            value = value.substring(0, end);
                            value = value.replaceAll("Acting", "");
                            value = sanitize(value);
                            if (NYAppealUtil.isProbablyName(value)) {
                                info.put(key, value);
                                break;
                            }
                        }
                    }

                    if (!value.isEmpty()) {
                        // also find ADA, which is next to DA, in parenthesis
                        int index = text.toLowerCase().indexOf("district attorney");
                        if (index > 0) {
                            m = DA_2_PATTERN.matcher(textFlow.substring(index));
                            if (m.find()) {
                                value = m.group();
                                value = inParentheses(value);
                                value =
                                        value.substring(0, value.length() - "of counsel".length());
                                value = sanitize(value);
                                info.put(DataKey.ADA, value);
                                stats.inc(DataKey.ADA, value);
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
                        info.put(key, sanitize(m.group()));
                    }

                    continue;

                case ProsecutMisconduct:
                    //regex = "prosecut[a-zA-Z\\s]*misconduct";
                    m = PROSECUTOR_MISCONDUCT_PATTERN.matcher(text);
                    if (m.find()) {
                        info.put(key, sanitize(m.group()));
                    }
                    continue;

                case Justice:
                    value = findAll(text, justice);
                    info.put(key, value);
                    if (!value.trim().isEmpty())
                        stats.inc(DataKey.Justice);
                    continue;

                default:
                    if (key.isOutputToFile()) {
                        logger.error(
                                "Aren't you forgetting something, Mr.? How about {} field?",
                                key.toString());
                    }

            }

        }
        boolean gapParsed = false;
        if (info.containsKey(DataKey.FirstDate) && info
                .containsKey(DataKey.AppealDate)) {
            String firstDateStr = info.get(DataKey.FirstDate);
            String appealDateStr = info.get(DataKey.AppealDate);
            Date firstDate = null;
            Date appealDate = null;
            if (!firstDateStr.isEmpty()) {
                try {
                    firstDate = dateFormat.parse(firstDateStr);
                } catch (NumberFormatException | ParseException e) {
                    logger.error("Date parsing error for {} in {}", firstDateStr,
                            file.getName());
                }
            }
            if (!appealDateStr.isEmpty()) {
                try {
                    appealDate = dateFormat.parse(appealDateStr);
                } catch (NumberFormatException | ParseException e) {
                    logger.error("Date parsing error for {} in {}", appealDateStr,
                            file.getName());
                }
            }
            if (firstDate != null && appealDate != null) {
                long gap = appealDate.getTime() - firstDate.getTime();
                int gapDays = (int) (gap / 1000 / 60 / 60 / 24);
                if (gapDays > 0) {
                    info.put(DataKey.Gap_days, Integer.toString(gapDays));
                    gapParsed = true;
                }
            }
        }
        if (gapParsed) {
            stats.inc(DataKey.Gap_days);
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

    private String sanitize(String value) {
        // remove all random occurrences of the separator
        value = value.replaceAll("\\" + CommonConstants.CSV_FIELD_SEPARATOR, "");
        // limit the length
        if (value.length() > MAX_FIELD_LENGTH)
            value = value.substring(0, MAX_FIELD_LENGTH - 1) + "...";
        // take out new lines
        value = value.replaceAll("\\r\\n|\\r|\\n|\"", " ");
        value = value.trim();
        if (value.endsWith(","))
            value = value.substring(0, value.length() - 1);
        return value;
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
            if (m.find())
                results.append(sanitize(m.group())).append(";");
        }
        if (results.length() > 0)
            results.deleteCharAt(results.length() - 1);
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


