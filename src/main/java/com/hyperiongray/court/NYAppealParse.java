package com.hyperiongray.court;

/**
 * Created by mark on 5/20/15.
 */
public class NYAppealParse {
    String extracts[][] = {
            {"Casenumber", "regex"},
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
}

