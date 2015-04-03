package com.opr.seekingalpha;

import org.junit.Test;

import static org.junit.Assert.*;

public class TranscriptStatsTest {

    @Test
    public void testUpdateCount() throws Exception {
        TranscriptStats instance = new TranscriptStats();
        instance.updateCount("AAPL", "2014");
        assertEquals("Updating count should increase the map size", instance.getTranscriptMap().size(), 1);
        instance.updateCount("AAPL", "2014");
        assertEquals("Updating count again should not increase the map size", instance.getTranscriptMap().size(), 1);
        assertEquals("Updated count should by now be total", instance.getCount("AAPL", "2014"), 2);
    }
    @Test
    public void testJsonParse() throws Exception {
        JsonParseTranscript instance = new JsonParseTranscript();
        TranscriptStats stats = instance.parseEarningsTranscript("data/earnings_transcripts.json");
        assertEquals("Verify parsed counts", 3, stats.getCount("AAPL", "2014"));
        assertEquals("Verify parsed counts", 4, stats.getCount("AAPL", "2013"));
    }
    @Test
    public void testJsonFormat() {
        TranscriptStats stats = new TranscriptStats();
        stats.updateCount("AAPL", "2014");
        stats.updateCount("AAPL", "2014");
        stats.updateCount("AAPL", "2013");
        stats.updateCount("AAPL", "2013");
        stats.updateCount("MSFT", "2014");
        stats.updateCount("MSFT", "2014");
        stats.updateCount("MSFT", "2013");
        stats.updateCount("MSFT", "2013");

        JsonParseTranscript instance = new JsonParseTranscript();
        String json = instance.format(stats);
        assertFalse(json.isEmpty());
    }
}