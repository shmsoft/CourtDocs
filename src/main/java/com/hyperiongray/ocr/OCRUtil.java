package com.hyperiongray.ocr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * Class OCRUtil.
 *
 * @author ilazarov
 *
 */
public class OCRUtil {
    private static AtomicLong incrementor = new AtomicLong(1);

    public static String createUniqueFileName(String out) {
        return out + System.currentTimeMillis() + "-" + incrementor.getAndIncrement();
    }

    public static String readFileContent(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    public static double compareText(String text, String source) {
        Map<String, Integer> sourceMap = parseTextToWords(source.toLowerCase());
        Map<String, Integer> textMap = parseTextToWords(text.toLowerCase());

        int total = 0;
        int notFound = 0;
        for (Map.Entry<String, Integer> textEntry : textMap.entrySet()) {
            String word = textEntry.getKey();
            int count = textEntry.getValue();

            Integer sourceCount = sourceMap.get(word);
            int sCount = sourceCount != null ? sourceCount.intValue() : 0;

            total += count;
            notFound += (count > sCount) ? count - sCount : 0;
        }

        return (double) (total - notFound) / (double) total;
    }

    private static Map<String, Integer> parseTextToWords(String source) {
        Map<String, Integer> words = new HashMap<>();
        String[] sourceLines = source.split("\n");
        for (String sourceLine : sourceLines) {
            String[] sourcePhrases = sourceLine.split(" ");

            for (String sourcePhrase : sourcePhrases) {
                String[] sourceWords = sourcePhrase.split(",");

                for (String sourceWord : sourceWords) {
                    sourceWord = sourceWord.trim();

                    Integer value = words.get(sourceWord);
                    if (value == null) {
                        value = 0;
                    }
                    ++value;
                    words.put(sourceWord, value);
                }
            }
        }

        return words;
    }
}
