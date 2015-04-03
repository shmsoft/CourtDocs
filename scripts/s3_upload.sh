#!/bin/bash
# This script is for FactSet uploads to S3
# The arguments
#        argv1 - destination bucket on S3 (hedge-iq)
#        argv2 - AWS ID (AKIAJSGS7DOH6GBCQJAA)
#        argv3 - AWS secret key (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)
#        argv4 - folder from which to upload  (testdata/upload). TODO - what to do with the uploaded files?
#        argv5 - folder in the bucket to which we upload (uploads). Note - the datestamp will be created in this folder.

java -cp target/FinShred-1.0-SNAPSHOT-jar-with-dependencies.jar com.opr.s3.S3Agent \
hedge-iq AKIAJSGS7DOH6GBCQJAA n+H0ap8ZGLR/DTSGJD1XyXO8nzKKf9i7dDIM5gkS target/upload uploads 