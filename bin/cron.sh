#!/bin/bash
startdate=`date -d "-1 month -$(($(date +%d)-1)) days" +"%Y-%m-%d"`
enddate=`date -d "-$(date +%d) days" +"%Y-%m-%d"`

if [ -z "$1" ]; then
   echo "Ini file required."
else
   ini_file="$1"
fi

echo "Running analytics for $startdate to $enddate using $ini_file"
#java gov.nysenate.analytics.Main --start_date $startdate --end_date $enddate --ini_file $ini_file
