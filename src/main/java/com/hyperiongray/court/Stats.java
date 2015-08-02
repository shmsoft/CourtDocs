package com.hyperiongray.court;

import java.text.DecimalFormat;
import java.util.Date;

public class Stats {
    public int docs;
    public int caseNumber;
    public int filesInDir;
    public int metadata;
    public int judge;
    public int judges;
    public int court;
    public int districtAttorneyProblem;
    public int adaProblem;
    public int gapDays;
    public int civil;
    public int criminal;
    public int sexOffence;
    public int crimes;
    public int county;
    public int keywords;
    public int firstDate;
    public int appealDate;
    public int modeOfConviction;
    public int fileNumber = 0;

    private Date startDate = new Date();

    DecimalFormat df = new DecimalFormat("####0.0");
    public String toString() {
        return
                "Parsing success stats:\n" +
                        "Files in dir: " + filesInDir + "\n" +
                        "Docs processed : " + ratio(docs, filesInDir) + "%\n" +
                        "Case number: " + ratio(caseNumber, filesInDir) + "%\n" +
                        "Metadata extracted: " + ratio(metadata, filesInDir) + "%\n" +
                        "Civil: " + ratio(civil, filesInDir) + "%\n" +
                        "Criminal: " + ratio(criminal, filesInDir) + "%\n" +
                        "Court: " + ratio(court, filesInDir) + "%\n" +
                        "Gap days: " + ratio(gapDays, filesInDir) + "%\n" +
                        "First date: " + ratio(firstDate, filesInDir) + "%\n" +
                        "Appeal date: " + ratio(appealDate, filesInDir) + "%\n" +
                        "Judge: " + ratio(judge, filesInDir) + "%\n" +
                        "Other judges present: " + ratio(judges, filesInDir) + "%\n" +
                        "District attorney: " + success(districtAttorneyProblem, criminal) + "%\n" +
                        "Assistant district attorney: " + success(adaProblem, criminal) + "\n" +
                        "Sex offender: " + ratio(sexOffence, filesInDir) + "%\n" +
                        "Crimes: " + ratio(crimes, criminal) + "%\n" +
                        "County: " + ratio(county, filesInDir) + "%\n" +
                        "Mode of conviction: " + ratio(modeOfConviction, criminal) + "%\n" +
                        "Keywords: " + ratio(keywords, filesInDir) + "%\n" +
                        "Number of output files: " + fileNumber + "\n" +
                        "Runtime: " + ( (new Date().getTime() - startDate.getTime()) / 1000 + " seconds");
    }
    private String success(int problems, int total) {
        return df.format(100. * (total - problems) / total);
    }
    private String ratio(int good, int total) {
        return df.format(100. * good / total);
    }
}
