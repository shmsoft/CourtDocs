package com.opr.finshred.parse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mark
 */
public class ParseUtil {

    /**
     * Get a string found between two delimiters.
     *
     * @param str string to analyze
     * @param del1 start delimiter
     * @param del2 end delimiter
     * @param start start reading the string from here
     * @return substring found between two delimiters
     */
    public static String getBetweenDelimiters(String str, String del1, String del2, int start) {
        String carve = "";
        int begin = str.indexOf(del1, start);
        if (begin >= 0) {
            int end = str.indexOf(del2, begin + del1.length());
            if (end >= 0) {
                carve = str.substring(begin + del1.length(), end);
            }
        }
        return carve;
    }

    /**
     * Get a series of strings found between two delimiters in the complete parsed text.
     * @param text text to analyze
     * @param del1 start delimiter
     * @param del2 end delimiter
     * @return strings found between two delimiters
     */
    public static List<String> getAllBetweenDelimiters(String text, String del1, String del2) {
        List<String> carves = new ArrayList<>();
        int start = 0;
        while (start >= 0) {
            int begin = text.indexOf(del1, start);
            if (begin >= 0) {
                int end = text.indexOf(del2, begin + del1.length());
                if (end >= 0) {
                    carves.add(text.substring(begin + del1.length(), end));
                }
                start = end + del2.length();
            } else {
                start = -1;
            }            
        }
        return carves;
    }
}
