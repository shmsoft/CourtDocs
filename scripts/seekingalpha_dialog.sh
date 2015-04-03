#!/bin/bash
# To compile and assemble to code for running the script, do this:
# mvn clean install assembly:single
#
# This script is for downloading from SeekingAlpha
# The arguments
#        argv1 - ...
#        right now, no arguments
java -cp target/FinShred-1.0-SNAPSHOT-jar-with-dependencies.jar com.opr.seekingalpha.SeekingAlphaDialog

