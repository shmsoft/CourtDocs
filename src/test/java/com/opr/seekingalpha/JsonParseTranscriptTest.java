package com.opr.seekingalpha;

import org.junit.Test;

public class JsonParseTranscriptTest {
    @Test
    public void testParse() {
        String [] args = new String[1];
//        args[0] = "data/earnings_transcripts.json";
//        JsonParseTranscript.main(args);
        args[0] = "data/earnings_transcripts.json";
        JsonParseTranscript.main(args);
    }
}