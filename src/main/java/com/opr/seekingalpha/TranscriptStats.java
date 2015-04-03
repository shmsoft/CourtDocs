package com.opr.seekingalpha;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mark on 12/23/14.
 */
public class TranscriptStats {
    private Map <String, Map<String, Integer>> transcriptMap = new HashMap<>();

    public Map<String, Map<String, Integer>> getTranscriptMap() {
        return transcriptMap;
    }

    public void updateCount(String ticker, String year) {
        Map <String, Integer> tickerMap;
        if (transcriptMap.containsKey(ticker)) {
            tickerMap = transcriptMap.get(ticker);
        } else {
            tickerMap = new HashMap<>();
            transcriptMap.put(ticker, tickerMap);
        }
        int count = 1;
        if (tickerMap.containsKey(year)) {
            count = tickerMap.get(year) + 1;
        }
        tickerMap.put(year, new Integer(count));
    }
    public int getCount(String ticker, String year) {
        int count = 0;
        if (transcriptMap.containsKey(ticker)) {
            Map <String, Integer> tickerMap = transcriptMap.get(ticker);
            if (tickerMap.containsKey(year)) {
                count = tickerMap.get(year);
            }
        }
        return count;
    }
}
