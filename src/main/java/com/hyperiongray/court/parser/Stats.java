package com.hyperiongray.court.parser;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Stats {

	private Map<DataKey, Integer> stats = new HashMap<DataKey, Integer>();
    
//    public int judge;
//    public int judges;
//    public int court;
//    public int districtAttorneyProblem;
//    public int adaProblem;
//    public int gapDays;
//    public int civil;
//    public int criminal;
//    //    public int sexOffence;
//    public int crimes;
//    public int county;
//    public int keywords;
//    public int firstDate;
//    public int appealDate;
//    public int modeOfConviction;

    private Date startDate = new Date();

    DecimalFormat df = new DecimalFormat("####0.0");

    public Stats() {
    	for (DataKey field : DataKey.values()) {
    		stats.put(field, 0);
    	}
    }
    
    public void inc(DataKey field, String value) {
    	if (!StringUtils.isEmpty(value)) {
    		stats.put(field, stats.get(field) + 1);
    	}
    }
    
    public void inc(DataKey field) {
    	stats.put(field, stats.get(field) + 1);
	}
    
    public int get(DataKey field) {
    	return stats.get(field);
    }
    
    public String toString() {
        return
                "Parsing success stats:\n" +
                        "Files in dir: " + get(DataKey.FiledInDir) + "\n" +
                        "Docs processed : " + ratio(get(DataKey.Docs), get(DataKey.FiledInDir)) + "%\n" +
                        "Case number: " + ratio(get(DataKey.Casenumber), get(DataKey.FiledInDir)) + "%\n" +
                        "Metadata extracted: " + ratio(get(DataKey.Metadata), get(DataKey.FiledInDir)) + "%\n" +
                        "Civil: " + ratio(get(DataKey.Civil), get(DataKey.FiledInDir)) + "%\n" +
                        "Criminal: " + ratio(get(DataKey.Criminal), get(DataKey.FiledInDir)) + "%\n" +
                        "Court: " + ratio(get(DataKey.Court), get(DataKey.FiledInDir)) + "%\n" +
                        "Gap days: " + ratio(get(DataKey.Gap_days), get(DataKey.FiledInDir)) + "%\n" +
                        "First date: " + ratio(get(DataKey.FirstDate), get(DataKey.FiledInDir)) + "%\n" +
                        "Appeal date: " + ratio(get(DataKey.AppealDate), get(DataKey.FiledInDir)) + "%\n" +
                        "Judge: " + ratio(get(DataKey.Judge), get(DataKey.FiledInDir)) + "%\n" +
                        "Other judges present: " + ratio(get(DataKey.Judges), get(DataKey.FiledInDir)) + "%\n" +
                        "District attorney: " + success(get(DataKey.DistrictAttorneyProblem), get(DataKey.Criminal)) + "%\n" +
                        "Assistant district attorney: " + success(get(DataKey.AdaProblem), get(DataKey.Criminal)) + "%\n" +
                        "Crimes: " + ratio(get(DataKey.Crimes), get(DataKey.Criminal)) + "%\n" +
                        "County: " + ratio(get(DataKey.County), get(DataKey.FiledInDir)) + "%\n" +
                        "Mode of conviction: " + ratio(get(DataKey.ModeOfConviction), get(DataKey.Criminal)) + "%\n" +
                        "Keywords: " + ratio(get(DataKey.Keywords), get(DataKey.FiledInDir)) + "%\n" +
                        "Interest of justice: " + ratio(get(DataKey.Justice), get(DataKey.FiledInDir)) + "%\n" +
                        "Number of output files: " + (get(DataKey.FileNumber) + 1) + "\n" +
                        "Runtime: " + ((new Date().getTime() - startDate.getTime()) / 1000 + " seconds");
    }

    private String success(int problems, int total) {
        return df.format(100. * (total - problems) / total);
    }

    private String ratio(int good, int total) {
        return df.format(100. * good / total);
    }

	public void set(DataKey key, int length) {
		stats.put(key, length);
	}

}
