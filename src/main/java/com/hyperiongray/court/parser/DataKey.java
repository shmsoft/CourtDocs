package com.hyperiongray.court.parser;

public enum DataKey {


//	public int docs;
//    public int filesInDir;
//    public int metadata;
//    public int fileNumber = 0;
//    
	 File,
	 Docs(false),
	 FiledInDir(false),
	 Metadata(false),
	 FileNumber(false),
	 Casenumber,
     CaseName,
	 CivilKriminal,
	 Court,
	 County,
	 Judge,
	 DistrictAttorney,
	 ADA, // assistant district attorney
     Keywords,
     GroundsForAppeal,
     Unanimous,
     FirstDate,
     AppealDate,
     Gap_days,
     ModeOfConviction,
     Crimes,
     Judges,
     Defense,
     DefendantAppellant,
     DefendantRespondent,
     HarmlessError,
     ProsecutMisconduct,
     DocumentLength, 
     Criminal (false),
     Civil (false),
     DistrictAttorneyProblem (false),
     Justice;
     
     
     private boolean outputToFile;
	
     public boolean isOutputToFile() { return outputToFile; }
     
     DataKey() {
    	 this(true);
     }
     
     DataKey(boolean outputToFile) {
		 this.outputToFile = outputToFile;
	 }
}
