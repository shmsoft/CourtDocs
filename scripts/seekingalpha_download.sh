#!/bin/bash
# To compike and assemble to code for running the script, do this:
# mvn clean install assembly:single
#
# This script is for downloading from SeekingAlpha
# The arguments
#        argv1 - ...
#        right now, no arguments
java -cp target/FinShred-1.0-SNAPSHOT-jar-with-dependencies.jar com.opr.seekingalpha.SeekingAlphaDownload

# To upload to S3, go to the output directory and
# s3cmd put --recursive --mime-type text/html earnings_transcripts/ s3://opr-solr-data/earnings_transcripts/
# other mime types: application/json
