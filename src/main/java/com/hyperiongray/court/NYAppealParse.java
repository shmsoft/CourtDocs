package com.hyperiongray.court;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mark on 5/20/15.
 */
public class NYAppealParse {
    public static String extracts[][] = {
            {"Casenumber", "\\[.+\\]"},
            {"DocumentLength", "regex"},
            {"Court", "regex"},
            {"County", "regex"},
            {"Judge", "regex"},
            {"Keywords", "regex"},
            {"FirstDate", "regex"},
            {"AppealDate", "regex"},
            {"Gap_days", "regex"},
            {"ModeOfConviction", "regex"},
            {"Crimes", "regex"},
            {"Judges", "regex"},
            {"Defense", "regex"},
            {"DefendantAppellant", "regex"},
            {"DefendantRespondent", "regex"},
            {"DistrictAttorney", "regex"}
    };

    public String toString() {
        StringBuffer buffer = new StringBuffer("Extractor\nField:Regex\n");
        for (int row = 0; row < extracts.length; ++row) {
            buffer.append(extracts[row][0]).append(":").append(extracts[row][1]).append("\n");
        }
        return buffer.toString();
    }
    public Map<String, String> extractInfo(String text) {
        Map <String, String> info = new HashMap<>();
        for (int e = 0; e < extracts.length; ++e) {
            String key = extracts[e][0];
            String regex = extracts[e][1];
            Matcher m = Pattern.compile(regex).matcher(text);
            if (m.find()) {
                String value = m.group();
                info.put(key, value);
            }
        }
        return info;
    }
}

