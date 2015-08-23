package com.hyperiongray.court;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mark on 5/6/15.
 * Util for NY Appeal web site doc download
 */
public class NYAppealUtil {
    private static final Logger logger = LoggerFactory.getLogger(NYAppealUtil.class);

    private static final String base = "http://www.courts.state.ny.us/reporter/slipidx/aidxtable";
    private static final List <String> months = Arrays.asList("january", "february", "march", "april",
            "may", "june", "july", "august", "september", "october", "november", "december");
    private static final String extension = ".shtml";

    private static final String linkRegex = "\\./3dseries.+\\.htm";
    private static final String downloadBase = "http://www.courts.state.ny.us/reporter/";
    
    private static String NAME_PLACEHOLDER = "%name%";
	private static Pattern INITIALS_PATTERN_1 = Pattern.compile("(\\w{3,}, [A-Z]\\.[A-Z]\\.)"); // Prudenti P.J.
	private static Pattern INITIALS_PATTERN_2 = Pattern.compile("([A-Z]\\. \\w{3,}, [A-Z]{2,2}\\.)"); // S. Miller JJ.
	private static Pattern INITIALS_PATTERN_3 = Pattern.compile("(\\w{3,}, [A-Z]{2,2}\\.)"); // Miller JJ.
	private static Pattern INITIALS_PATTERN_4 = Pattern.compile("(\\w{3,} JR\\.)", Pattern.CASE_INSENSITIVE); // Egan Jr.
	private static Pattern INITIALS_PATTERN_5 = Pattern.compile("(\\w{3,}, JR\\., [A-Z]\\.[A-Z]\\.)", Pattern.CASE_INSENSITIVE); // Egan, JR., S.J.,
	private static Pattern INITIALS_PATTERN_6 = Pattern.compile("(\\w{3,} [A-Z]\\. \\w{3,})", Pattern.CASE_INSENSITIVE); // Michael C. Green
	private static Pattern INITIALS_PATTERN_7 = Pattern.compile("(\\w{3,} \\w{3,}, J\\.)", Pattern.CASE_INSENSITIVE); // William Smith J.

	private static Pattern[] INITIAL_PATTERNS = { INITIALS_PATTERN_5, INITIALS_PATTERN_1, INITIALS_PATTERN_2, INITIALS_PATTERN_3, INITIALS_PATTERN_4, INITIALS_PATTERN_6, INITIALS_PATTERN_7 };
	private static List<String> countiesList;
	
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
    public static void sleep(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            logger.info("Sleep interrupted");
        }
    }
    
    public static List<String> splitToSentences(String text) {
    	List<SearchResult> replacedPatterns = new ArrayList<SearchResult>();
    	// temporarily replace names with initials to placeholder so it is easier to split text to sentences
    	for (Pattern pattern : INITIAL_PATTERNS) {
    		List<SearchResult> results = findNamesWithInitials(text, pattern);
    		for (SearchResult r : results) {
    			if (text.indexOf(r.text) >= 0) {
	    			text = text.replaceFirst(r.text, NAME_PLACEHOLDER);
	    			replacedPatterns.add(r);
    			}
    		}
    	}
    	Collections.sort(replacedPatterns, new Comparator<SearchResult>() {
			@Override public int compare(SearchResult o1, SearchResult o2) {
				return Integer.compare(o1.from, o2.from);
			}
    	});
    	String[] sentences = text.split("(?<=\\.\"?|\\s{2})");
    	int currentNameIdx = 0;
    	List<String> ret = new ArrayList<String>();
    	// put names back
    	for (String sentence : sentences) {
    		int from = 0;
    		int idx;
    		while ((idx = sentence.indexOf(NAME_PLACEHOLDER, from)) >= 0) {
    			sentence = sentence.replaceFirst(NAME_PLACEHOLDER, replacedPatterns.get(currentNameIdx++).text);
    			from = idx + 1;
    		}
    		sentence = sentence.trim();
    		if (!sentence.isEmpty()) {
    			ret.add(sentence);
    		}
    	}
    	return ret;
    }
    
	public static List<SearchResult> findNamesWithInitials(String line, Pattern pattern) {
		Matcher m = pattern.matcher(line);
		int start = 0;
		List<SearchResult> ret = new ArrayList<SearchResult>();
		while (m.find(start)) {
			String match = m.group();
			int rangeStart = m.start();
			int rangeEnd = m.end();
			start = m.end();
			ret.add(new SearchResult(rangeStart, rangeEnd, match));
		}
		return ret;
	}
	
	public static boolean isProbablyName(String s) {
		s = s.trim();
		if (s.isEmpty()) {
			return false;
		}
		String[] words = s.split(" ");
		int probablyNotNamePart = 0;
		int wordsCnt = 0;
		for (int i = 0; i < words.length; i++) {
			if (words[i].isEmpty()) {
				continue;
			}
			if (!Character.isUpperCase(words[i].charAt(0))) {
				probablyNotNamePart++;
			}
			if (!words[i].equalsIgnoreCase("the")) {
				wordsCnt++;
			}
		}
		if (wordsCnt == 0) {
			return false;
		}
		return probablyNotNamePart <= 1;
	}
	
	public static boolean isCounty(String s) {
		if (countiesList == null) {
			countiesList = loadCounties();
		}
		for (String county : countiesList) {
			if (isSameStrings(county, s)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isSameStrings(String s1, String s2) {
		return StringUtils.getLevenshteinDistance(s1.toLowerCase(), s2.toLowerCase()) <= 1;
	}
	
	private static List<String> loadCounties() {
		try {
			List<String> lines = FileUtils.readLines(new File(NYAppealUtil.class.getClassLoader().getResource("counties_list.txt").getFile()));
			return lines;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static class SearchResult {
		
		public int from, to;
		public String text;
		
		public SearchResult(int from, int to, String text) {
			this.from = from;
			this.to = to;
			this.text = text;
		}
	}
}
