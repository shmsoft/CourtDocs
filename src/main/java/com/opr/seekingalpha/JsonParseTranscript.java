package com.opr.seekingalpha;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by mark on 12/22/14.
 */
public class JsonParseTranscript {

    public static void main(String[] args) {
        String filePath = args[0];
        JsonParseTranscript instance = new JsonParseTranscript();
        instance.parseEarningsTranscript(filePath);
    }

    public TranscriptStats parseEarningsTranscript(String filePath) {
        TranscriptStats stats = new TranscriptStats();
        try {
            // read the json file
            FileReader reader = new FileReader(filePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            Set keySet = jsonObject.keySet();
            String[] tickers = (String[]) keySet.toArray(new String[0]);
            for (String ticker : tickers) {
                JSONObject earningsTranscript = (JSONObject) ((JSONObject) jsonObject.get(ticker)).get("earnings_transcripts");
                String[] years = (String[]) earningsTranscript.keySet().toArray(new String[0]);
                for (String year: years) {
                    Long count = (Long) earningsTranscript.get(year);
                    // I do not want the "setCount" method, because it would not be used
                    for (int c = 0; c < count; ++c) {
                        stats.updateCount(ticker, year);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return stats;
    }

    /**
     * Form JSON description of downloads. For this simple object, do it directly
     * @param stats TranscriptStats object to convert to json
     * @return json representation
     */
    public String format(TranscriptStats stats) {
        String nl = "\n";
        StringBuilder builder = new StringBuilder();
        builder.append("{").append(nl);
        Map<String, Map<String, Integer>> transcriptMap =  stats.getTranscriptMap();
        String[] tickers = transcriptMap.keySet().toArray(new String[0]);
        for (String ticker: tickers) {
            builder.append("\"" + ticker + "\": {\n" +
                    "    \"earnings_transcripts\": {").append(nl);
            Map<String, Integer> transcripts = transcriptMap.get(ticker);
            String [] years = transcripts.keySet().toArray(new String[0]);
            for (String year: years) {
                builder.append("\"" + year + "\"" + ": " +  transcripts.get(year) + ",");
            }
            // take out the last comma, it is extra
            builder.deleteCharAt(builder.length() - 1);
            builder.append("}\n" +
                    "  },");
        }
        // take out the last comma, it is extra
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }
}

