# NY Appeal Courts - web site schema for document crawling

## URL structure

Here is a sample URL

http://www.courts.state.ny.us/reporter/slipidx/aidxtable_1_2003_october.shtml

* Base: http://www.courts.state.ny.us/reporter/slipidx/aidxtable_
* Court: one digit, 1 through 4
* Year: 2003 through 2015
* Month: lowercase month name
* Extension: shtml

## To run the program

To obtain help, run this command

    java -cp target/CourtDoc-1.0-SNAPSHOT-jar-with-dependencies.jar com.hyperiongray.court.NYAppealCollect
    
You will get the following help
    
    NYAppealCollect - downloads and summarize PDF reports
    -o,--outputDir <arg>   Output directory
    -s,--sample <arg>      Sample the data, i.e. .01 means download only 1% of all files
    -v,--verify            Verify that we are hitting the right urls, do no downloads
    
## To build the project
    
    mvn assembly:single
