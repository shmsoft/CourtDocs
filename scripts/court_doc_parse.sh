#!/bin/sh
java -cp target/CourtDoc-1.0-SNAPSHOT-jar-with-dependencies.jar \
com.hyperiongray.court.Application \
-i ../court_docs/downloads/txt/ -o ../court_docs/downloads/parsed
