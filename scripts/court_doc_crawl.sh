#!/bin/sh
java -cp target/CourtDoc-1.0-SNAPSHOT-jar-with-dependencies.jar \
com.hyperiongray.court.NYAppealCollect \
-o ../memex_data/court_docs/2015-2017-verify
