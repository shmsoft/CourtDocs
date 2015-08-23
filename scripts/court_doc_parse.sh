#!/bin/sh
#FILE_NAME=/home/mark/projects/BDE/data/zip-codes/esiid_zipcode.csv
FILE_NAME=~/jim/esiid_zipcode.csv
java -cp target/bde_smt-1.0-SNAPSHOT-jar-with-dependencies.jar com.bde.hbase.HBaseOps --type zip --input $FILE_NAME --environment cloudera
