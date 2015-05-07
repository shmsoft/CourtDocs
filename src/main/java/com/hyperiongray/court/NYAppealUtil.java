package com.hyperiongray.court;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mark on 5/6/15.
 * Util for NY Appeal web site doc download
 */
public class NYAppealUtil {
    private static final String base = "http://www.courts.state.ny.us/reporter/slipidx/aidxtable";
    private static final List <String> months = Arrays.asList("january", "february", "march", "april",
            "may", "june", "july", "august", "september", "october", "november", "december");
    private static final String extension = ".shtml";

    private static final String linkRegex = "\\./3dseries.+\\.htm";
    private static final String downloadBase = "http://www.courts.state.ny.us/reporter/";

    /**
     * Takes into the years actually available on the site
     * @return list of all URLs to download
     *
     */

    public List<String> listURLs() {
        List<String> list = new ArrayList<>();
        int currentYear = 2015; // can modify this to calculate, later if we keep using the code
        for (int court = 1; court <= 4; ++court) {
            for (int year = 2003; year <= currentYear; ++year) {
                for (String month: months) {
                    list.add(base + "_" +  court + "_" + year + "_" + month + extension);
                }
            }
        }
        return list;
    }
    public List<String> listDownloadLinks(String htmlPage) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile(linkRegex).matcher(htmlPage);
        while (m.find()) {
            list.add(downloadBase + m.group());
        }
        return list;
    }
}
