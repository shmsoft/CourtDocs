# CourtDocs

## Capabilities

#### Pulling data from California rehab institutions
#### Pulling decisions from NY Courts of appeal
* Crawling the 4 courts documents, downloading them and converting them from HTML to TXT
* Parsing the docs, to summarize for stats (in progress)


Please note that all documentation is found [here](https://github.com/TeamHG-Memex/CourtDocs/tree/master/doc) in this project

## Data

About 100K of appeal documents scraped from the NY State Court of appeals are found in S3 
[here](https://s3-us-west-2.amazonaws.com/darpa-memex/CourtDocs/court_documents.tar)

The (hopefully) latest results of processing, extracted with this CourtDoc regex's are 
[here](https://s3-us-west-2.amazonaws.com/darpa-memex/CourtDocs/CourtDocs-Output.zip)

Latest stats

    Files in dir: 111018
    Docs processed : 100.0%
    Case number: 100.0%
    Metadata extracted: 100.0%
    Civil: 71.0%
    Criminal: 29.0%
    Court: 94.7%
    Gap days: 90.6%
    First date: 90.7%
    Appeal date: 100.0%
    Judge: 85.8%
    Other judges present: 100.0%
    District attorney: 0.0%
    Assistant district attorney: 100.0
    Crimes: 55.8%
    County: 92.8%
    Mode of conviction: 53.9%
    Keywords: 93.3%
    Number of output files: 12
    Runtime: 1732 seconds