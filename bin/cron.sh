#!/bin/bash

source $(dirname "$0")/utils.sh

startdate=`date -d "-1 month -$(($(date +%d)-1)) days" +"%Y-%m-%d"`
enddate=`date -d "-$(date +%d) days" +"%Y-%m-%d"`

if [ -z "$1" ]; then
   echo "Ini file required."
   exit
else
   inifile="$1"
fi

echo "Running analytics for $startdate to $enddate using $inifile"
java -jar $ROOTDIR/target/analytics-$VERSION.jar --start-date $startdate --end-date $enddate --ini-file $inifile
