package com.hyperiongray.court;

/**
 * Created by mark on 5/28/15.
 */
public class Stats {
    public int docs;
    public int caseProblem;
    public int filesInDir;
    public int metadata;
    public int judgeProblem;
    public int courtProblem;
    public int districtAttorneyProblem;
    public int gapDays;

    public String toString() {
        return
                "Parsing stats:\n" +
                        "Files in dir: " + filesInDir + "\n" +
                        "Docs processed: " + docs + "\n" +
                        "Metadata lines written: " + metadata + "\n" +
                        "Case parsing problems: " + caseProblem + "\n" +
                        "Court parsing problems: " + courtProblem + "\n" +
                        "Gap days: " + gapDays + "\n" +
                        "Judge parsing problems: " + judgeProblem + "\n" +
                        "District attorney parsing problems: " + districtAttorneyProblem;
    }
}
