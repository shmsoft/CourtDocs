package com.opr.seekingalpha;

/**
 * Collect article extract setting in one place.
 *
 * @author mark
 */
public class ExtractSettings {
    private String tableOfContents = "../../data/seekingalpha/it_articles_new.csv";
    private String articlesContents = "../../data/seekingalpha/sa_articles_new.csv";
    ;
    private String outputDir = "../../data/seekingalpha/earnings_transcripts";
    private int docLimit;
    private boolean transcripts = true;
    private boolean earningsCalls = true;
    private String companyName = "";
    private String filters =
            // exclusions first
            "!Press Conference Call\n" +
                    "!Annual Meeting of Shareholders\n" +
                    "!Meeting Conference Call\n" +
                    "!Event Conference Call\n" +
                    "!Technology Conference\n" +
                    "!Financial Analyst Meeting Call" +
                    "Earnings Call Transcript\n" +
                    "conference call transcript\n" +
                    "earnings call transcript\n" +
                    "Earnings Release Conference Call\n" +
                    "Earnings Conference Call\n" +
                    "Earnings Call\n" +
                    "Financial Analysts Call\n";

    private static final ExtractSettings instance = new ExtractSettings();

    private ExtractSettings() {
    }

    /**
     * @return
     */
    public static ExtractSettings getInstance() {
        return instance;
    }

    /**
     * @return the tableOfContents
     */
    public String getTableOfContents() {
        return tableOfContents;
    }

    /**
     * @param tableOfContents the tableOfContents to set
     */
    public void setTableOfContents(String tableOfContents) {
        this.tableOfContents = tableOfContents;
    }

    /**
     * @return the articlesContents
     */
    public String getArticlesContents() {
        return articlesContents;
    }

    /**
     * @param articlesContents the articlesContents to set
     */
    public void setArticlesContents(String articlesContents) {
        this.articlesContents = articlesContents;
    }

    /**
     * @return the outputDir
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * @param outputDir the outputDir to set
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * @return the docLimit
     */
    public int getDocLimit() {
        return docLimit;
    }

    /**
     * @param docLimit the docLimit to set
     */
    public void setDocLimit(int docLimit) {
        this.docLimit = docLimit;
    }

    /**
     * @return the transcripts
     */
    public boolean isTranscripts() {
        return transcripts;
    }

    /**
     * @param transcripts the transcripts to set
     */
    public void setTranscripts(boolean transcripts) {
        this.transcripts = transcripts;
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * @return the filters
     */
    public String getFilters() {
        return filters;
    }

    /**
     * @return the filters
     */
    public String[] getFiltersAsArray() {
        if (filters == null || filters.trim().isEmpty()) {
            return new String[0];
        }
        String[] asArray = filters.split("\n");
        for (int i = 0; i < asArray.length; ++i) {
            asArray[i] = asArray[i].trim().toLowerCase();
        }
        return asArray;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(String filters) {
        this.filters = filters;
    }

    /**
     * @return the earningsCalls
     */
    public boolean isEarningsCalls() {
        return earningsCalls;
    }

    /**
     * @param earningsCalls the earningsCalls to set
     */
    public void setEarningsCalls(boolean earningsCalls) {
        this.earningsCalls = earningsCalls;
    }
}
