# CourtDocs

## Capabilities

#### Pulling data from California rehab institutions
#### Pulling decisions from NY Courts of appeal
* Crawling all documents from the the 4 courts of appeal, downloading them and converting them from HTML to TXT
* Parsing the docs, to summarize for stats.
#### Parsing the text appeal documents, producing a csv file with extracted attributes

To run the application(s), look into the 'scripts' folder


Please note that all documentation is found in the 'doc' folder in this project

## Data

About 100K of appeal documents scraped from the NY State Court of appeals are found in S3 
[here](https://s3-us-west-2.amazonaws.com/darpa-memex/CourtDocs/court_documents.tar)

The (hopefully) latest results of processing, extracted with this CourtDoc regex's are 
[here](https://s3-us-west-2.amazonaws.com/darpa-memex/CourtDocs/CourtDocs-Output.zip)

## Development
* For development I prefer IntelliJ.
    * It allows multiple configurations for running and debugging
    * Overall better, I can't say where NetBeans would exceed IntelliJ, except in the UI editor