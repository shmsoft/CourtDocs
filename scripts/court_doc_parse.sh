#!/bin/sh
# main docs
#java -cp target/CourtDoc-1.0-SNAPSHOT-jar-with-dependencies.jar \
#com.hyperiongray.court.Application \
#-i ../court_docs/downloads/txt/ -o ../court_docs/downloads/parsed
# new docs
java -cp target/CourtDoc-1.0-SNAPSHOT-jar-with-dependencies.jar \
com.hyperiongray.court.Application \
-i ../memex_data/court_docs/2015-2017/txt -o ../court_docs/downloads/parsed
