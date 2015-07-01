package com.hyperiongray.court;

/**
 * Created by mark on 5/28/15.
 */
public class Stats {
    public int docs;
    public int caseNumber;
    public int filesInDir;
    public int metadata;
    public int judge;
    public int judges;
    public int courtProblem;
    public int districtAttorneyProblem;
    public int gapDays;
    public int civil;
    public int criminal;
    public int sexOffence;
    public int crimes;

    public String toString() {
        return
                "Parsing success stats:\n" +
                        "Files in dir: " + filesInDir + "\n" +
                        "Docs processed : " + ratio(docs, filesInDir) + "%\n" +
                        "Case number: " + ratio(caseNumber, filesInDir) + "%\n" +
                        "Metadata extracted: " + ratio(metadata, filesInDir) + "%\n" +
                        "Civil: " + ratio(civil, filesInDir) + "%\n" +
                        "Criminal: " + ratio(criminal, filesInDir) + "%\n" +
                        "Court: " + success(courtProblem, filesInDir) + "%\n" +
                        "Gap days: " + success(gapDays, filesInDir) + "%\n" +
                        "Judge: " + ratio(judge, filesInDir) + "%\n" +
                        "Judges: " + ratio(judges, filesInDir) + "%\n" +
                        "District attorney: " + success(districtAttorneyProblem, criminal) + "%\n" +
                        "Sex offender: " + ratio(sexOffence, filesInDir) + "%\n" +
                        "Crimes: " + ratio(crimes, criminal) + "%\n";
    }
    private int success(int problems, int total) {
        return (int) 100. *(total - problems) / total;
    }
    private int ratio(int good, int total) {
        return (int) 100. * good / total;
    }

}
