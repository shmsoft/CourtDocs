package com.hyperiongray.court.parser.gate;

public enum GateAnnotation {

	ADA("Assistant_District_Attorney"),
	WITNESSING_JUDGE("Witnessing_Judge"),
	CASE_NAME("Case_Name"),
	CASE_NUMBER_TYPE_1("Case_Number_Type_1"),
	CASE_NUMBER_TYPE_2("Case_Number_Type_2"),
	MODE_OF_CONVICTION("ModeOfConviction"),
	DEFENDANT_APPELLANT("Defendant_Appellant"),
	DEFENDANT_RESPONDENT("Defendant_Respondent"),
	US_COUNTY("US_County"), 
	DATE_OF_FILING("Date_of_Filing"),
	APPEAL_DATE("Appeal_Date"),
	DISTRICT_ATTORNEY("US_District_Attorney"),
	CRIME("Crime"),
	UNANIMOUS_KEYWORD("Unanimous_Keyword"),
	US_COURT_TYPE("US_Court_Type"),
	GROUND_FOR_APPEAL_KEYWORD("Grounds_For_Appeal_Keyword"),
	PROSECUT_MISCONDUCT("Prosecut_Misconduct"),
	DEFENDER_TYPE("Defender_Type"),
	LEGAL_KEYWORD_IMPORTANT("Legal_Keyword_Important"),
	JUDGE("Judge")
	;
	
	// Type must match annotation name produced by JAPE compiler
	private final String type;
	
	GateAnnotation(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
