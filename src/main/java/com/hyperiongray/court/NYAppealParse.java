package com.hyperiongray.court;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mark on 5/20/15.
 */
public class NYAppealParse {
    private static final Logger logger = LoggerFactory.getLogger(NYAppealParse.class);
    public static String extracts[][] = {
            {"Casenumber", "\\[.+\\]"},
            {"DocumentLength", "regex"},
            {"Court", "regex"},
            {"County", "[a-zA-Z]+\\sCounty"},
            {"Judge", "Court \\(.+\\)"},
            {"Keywords", "regex"},
            {"FirstDate", "rendered.+\\."},
            {"AppealDate", "(January|February|March|April|May|June|July|August|September|October|November|December).+"},
            {"Gap_days", "regex"},
            {"ModeOfConviction", "plea\\s*of\\s*guilty|jury\\s*verdict|nonjury\\s*trial"},
            {"Crimes", "regex"},
            {"Judges", "Present.+"},
            {"Defense", "regex"},
            {"DefendantAppellant", "regex"},
            {"DefendantRespondent", "regex"},
            {"DistrictAttorney", "\\n[a-zA-Z,.\\s]+District Attorney"},
            {"HarmlessError", "regex"},
            {"NotHarmlessError", "regex"}
    };

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMdd,yyyy");

    public String toString() {
        StringBuffer buffer = new StringBuffer("Extractor\nField:Regex\n");
        for (int row = 0; row < extracts.length; ++row) {
            buffer.append(extracts[row][0]).append(":").append(extracts[row][1]).append("\n");
        }
        return buffer.toString();
    }

    public Map<String, String> extractInfo(String text) {
        Map<String, String> info = new HashMap<>();
        for (int e = 0; e < extracts.length; ++e) {
            String key = extracts[e][0];
            if (key.equals("DocumentLength")) continue;
            if (key.equals("Court")) continue;
            if (key.equals("GapDays")) continue;
            String regex = extracts[e][1];
            Matcher m = Pattern.compile(regex).matcher(text);
            if (m.find()) {
                String value = m.group();
                if (key.equals("DistrictAttorney")) value = value.substring(2);
                if (key.equals("Judges")) value = value.substring("Judges-".length() + 1);
                if (key.equals("Judge")) value = value.substring("Judge ".length() + 1, value.length() - 1);
                if (key.equals("FirstDate")) value = value.substring("rendered ".length(), value.length() - 1);
                info.put(key, value);
            }
        }
        info.put("DocumentLength", Integer.toString(text.length()));
        String court = text.contains("Supreme Court") ? "Supreme Court" : "County Court";
        info.put("Court", court);
        if (info.containsKey("FirstDate") && info.containsKey("AppealDate")) {
            String firstDateStr = info.get("FirstDate");
            String appealDateStr = info.get("AppealDate");
            try {
                Date firstDate = dateFormat.parse(firstDateStr);
                Date appealDate = dateFormat.parse(appealDateStr);
                long gap = appealDate.getTime() - firstDate.getTime();
                int gapDays = (int) (gap / 1000 / 60 / 60 / 24);
                if (gapDays > 0) {
                    info.put("Gap_days", Integer.toString(gapDays));
                }
            } catch (NumberFormatException | ParseException e) {
                logger.error("Parsing error for {} and/or {}", firstDateStr, appealDateStr);
            }
        }
        if (info.containsKey("ModeOfConviction")) {
            String mode = info.get("ModeOfConviction");
            // crime is from here till the end of the line
            int crimeStart = text.indexOf(mode);
            if (crimeStart > 0) {
                crimeStart += mode.length();
                int comma = text.indexOf("\n", crimeStart);
                if (comma > 0 && (comma - crimeStart < 5)) crimeStart += (comma - crimeStart + 1);
                int crimeEnd = text.indexOf(".", crimeStart);
                if (crimeEnd > 0) {
                    info.put("Crimes", text.substring(crimeStart, crimeEnd));
                }
            }
        }
        return info;
    }
}


