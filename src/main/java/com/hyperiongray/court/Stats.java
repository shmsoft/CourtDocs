package com.hyperiongray.court;

/**
 * Created by mark on 5/28/15.
 */
public class Stats {
    public int docs;
    public int noParse;
    public int caseProblem;
    public int filesInDir;
    public int metadata;

    public String toString() {
        return
                "Parsing stats:\n" +
                        "Files in dir: " + filesInDir + "\n" +
                        "Docs processed: " + docs + "\n" +
                        "Metadata lines written: " + metadata + "\n" +
                        "Case parsing problems: " + caseProblem;
    }
}
